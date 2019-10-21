package cn.how2j.trend.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.how2j.trend.pojo.AnnualProfit;
import cn.how2j.trend.pojo.IndexData;
import cn.how2j.trend.pojo.Profit;
import cn.how2j.trend.pojo.Trade;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.StrUtil;

@Service
public class SimulateService {
	@Autowired IndexDataService indexDataService;
	TimeInterval timer = DateUtil.timer();
	public Map<String,Object> simulate(int ma, float sellRate, float buyRate, float serviceCharge, List<IndexData> indexDatas)  {

		
		List<Profit> profits =new ArrayList<>();
		List<Trade> trades = new ArrayList<>();
		float initCash = 1000;
		
		
		float cash = initCash;
		float share = 0;
		float value = 0;

		
		float init =0;
		if(!indexDatas.isEmpty()) 
			init = indexDatas.get(0).getClosePoint();
		
		int winCount = 0;
		float totalWinRate = 0;
		float avgWinRate = 0;
		float totalLossRate = 0;
		int lossCount = 0;
		float avgLossRate = 0;
		
		int lastBuyPoint = -1;
		
		for (int i = 0; i<indexDatas.size() ; i++) {
			IndexData indexData = indexDatas.get(i);
//			closePoint
			float closePoint = indexData.getClosePoint();
			float avg = getMA(i,ma,indexDatas);
			float max = getMax(i,lastBuyPoint,ma,indexDatas);

			float increase_rate = closePoint/avg;
			float decrease_rate = closePoint/max;

//			System.out.printf("closePoint:%f\tmax:%f\tdecrease_rate:%f%n",closePoint,max,decrease_rate);
			
			
			if(avg!=0) {
				//buy
				 if(increase_rate>buyRate  ) {
					 if(0 == share) {
							share = cash / closePoint;
							cash = 0;
							
							Trade trade = new Trade();
							trade.setBuyDate(indexData.getDate());
							trade.setBuyClosePoint(indexData.getClosePoint());
							trade.setSellDate("n/a");
							trade.setSellClosePoint(0);
							trades.add(trade);
							
							lastBuyPoint = i;
					 }
				}				
				//sell
				else if(decrease_rate<sellRate && 0!= share) {
						cash = closePoint * share * (1-serviceCharge);
						share = 0;
						
						Trade trade =trades.get(trades.size()-1);
						trade.setSellDate(indexData.getDate());
						trade.setSellClosePoint(indexData.getClosePoint());
						
						if(trade.getSellClosePoint()-trade.getBuyClosePoint()>0) {
							totalWinRate +=(trade.getSellClosePoint()-trade.getBuyClosePoint())/trade.getBuyClosePoint();
							winCount++;
							
							
						}
						
						else {
							totalLossRate +=(trade.getSellClosePoint()-trade.getBuyClosePoint())/trade.getBuyClosePoint();
							lossCount ++;
						}
						
						
						float rate = cash / initCash;
						
						trade.setRate(rate);
						
				}

				//do nothing
				else{
					
				}
								
			}

			if(share!=0) {
				value = closePoint * share;
			}		
			else {
				value = cash;
			}
			float rate = value/initCash;
			
			avgWinRate = totalWinRate / winCount;
			avgLossRate = totalLossRate / lossCount;
			
			Profit profit = new Profit();
			profit.setDate(indexData.getDate());
			profit.setValue(rate*init);
			
			profits.add(profit);
						
		}
		
		float lastClosePoint = indexDatas.get(indexDatas.size()-1).getClosePoint();
		if(share!=0) {
			cash = lastClosePoint * share;
			share = 0;
		}		
		Map<String,Object> map = new HashMap<>();
		List<AnnualProfit> annualProfits = caculateAnnualProfits(indexDatas, profits);
		
		map.put("profits", profits);
		map.put("annualProfits", annualProfits);

		map.put("winCount", winCount);
		map.put("lossCount", lossCount);
		map.put("avgWinRate", avgWinRate);
		map.put("avgLossRate", avgLossRate);
		map.put("trades", trades);
		return map;
	}
	
	
	
	private List<AnnualProfit> caculateAnnualProfits(List<IndexData> indexDatas, List<Profit> profits) {
		List<AnnualProfit> result = new ArrayList<>();
		String strStartDate = indexDatas.get(0).getDate();
		String strEndDate = indexDatas.get(indexDatas.size()-1).getDate();
		
		Date startDate = DateUtil.parse(strStartDate);
		Date endDate = DateUtil.parse(strEndDate);
		
		int startYear = DateUtil.year(startDate);
		int endYear = DateUtil.year(endDate);
		
		for (int year =startYear; year <= endYear; year++) {
			AnnualProfit annualProfit = new AnnualProfit();
			annualProfit.setYear(year);
			
			float indexIncome = getIndexIncome(year,indexDatas);
			float trendIncome = getTrendIncome(year,profits);
			annualProfit.setIndexIncome(indexIncome);
			annualProfit.setTrendIncome(trendIncome);
			result.add(annualProfit);
			
		}
		
		
		
		return result;
	}



	private float getTrendIncome(int year, List<Profit> profits) {
		Profit first=null;
		Profit last=null;
		
		for (Profit profit : profits) {
			String strDate = profit.getDate();
			int currentYear = getYear(strDate);
			
			if(currentYear == year) {
				if(null==first)
					first = profit;
				last = profit;
			}
			if(currentYear > year)
				break;
			
			
		}
		


		
		return (last.getValue() - first.getValue()) / first.getValue(); 
	}







	private int getYear(String date) {
		String strYear= StrUtil.subBefore(date, "-", false);
		return Convert.toInt(strYear);
	}
	
	private float getIndexIncome(int year, List<IndexData> indexDatas) {
		IndexData first=null;
		IndexData last=null;
		
		for (IndexData indexData : indexDatas) {
			String strDate = indexData.getDate();
//			Date date = DateUtil.parse(strDate);
			int currentYear = getYear(strDate);
			
			if(currentYear == year) {
				if(null==first)
					first = indexData;
				last = indexData;
			}
		}
		


		
		return (last.getClosePoint() - first.getClosePoint()) / first.getClosePoint(); 
	}



	private static float getMax(int i, int lastBuyPoint, int day, List<IndexData> list) {
		int start = i-1-day;
		
		if(start<lastBuyPoint)
			start = lastBuyPoint;
		
		int now = i-1;
		
		
		if(start<0)
			return 0;
		
		float sum = 0;
		float max = 0;
		for (int j = start; j < now; j++) {
			IndexData bean =list.get(j);
			if(bean.getClosePoint()>max) {
				max = bean.getClosePoint();
			}
			
		}
		
		return max;
	}
	
	private static float getMA(int i, int ma, List<IndexData> list) {
		int start = i-1-ma;
		int now = i-1;
		
		
		if(start<0)
			return 0;
		
		float sum = 0;
		float avg = 0;
		for (int j = start; j < now; j++) {
			IndexData bean =list.get(j);
			sum += bean.getClosePoint();
			
		}
		avg = sum / (now - start);
		
		return avg;
//		println(start+ "\t" + now);
//		return 0;
	}	
		
}
