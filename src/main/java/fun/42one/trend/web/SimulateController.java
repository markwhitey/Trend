package cn.how2j.trend.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import cn.how2j.trend.pojo.AnnualProfit;
import cn.how2j.trend.pojo.IndexData;
import cn.how2j.trend.pojo.Profit;
import cn.how2j.trend.pojo.Trade;
import cn.how2j.trend.service.IndexDataService;
import cn.how2j.trend.service.SimulateService;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.StrUtil;
 
@RestController
public class SimulateController {
	@Autowired IndexDataService indexDataService;
	@Autowired SimulateService simulateService;
	
	@GetMapping("/simulate/{code}/{ma}/{buyThreshold}/{sellThreshold}/{serviceCharge}/{startDate}/{endDate}")
	public Map<String,Object> list(@PathVariable("code") String code
			,@PathVariable("ma") int ma
			,@PathVariable("buyThreshold") float buyThreshold
			,@PathVariable("sellThreshold") float sellThreshold
			,@PathVariable("serviceCharge") float serviceCharge
			,@PathVariable("startDate") String strStartDate
			,@PathVariable("endDate") String strEndDate
			
			) throws Exception {
		

		
		TimeInterval timer = DateUtil.timer();		

		

		List<IndexData> allIndexDatas = indexDataService.listIndexData(code);
		


		String startDate = allIndexDatas.get(0).getDate();
		String endDate = allIndexDatas.get(allIndexDatas.size()-1).getDate();		
		

		allIndexDatas = filterByDateRange(allIndexDatas,strStartDate, strEndDate);
		

		
		

		
		
//		float sellRate = 0.90f;
//		float buyRate = 1.05f;
		
		float sellRate = sellThreshold;
		float buyRate = buyThreshold;
		


		Map<String,?> simulateResult= simulateService.simulate(ma,sellRate, buyRate,serviceCharge, allIndexDatas);
		
		

		
		int winCount = (Integer) simulateResult.get("winCount");
		int lossCount = (Integer) simulateResult.get("lossCount");
		float avgWinRate = (Float) simulateResult.get("avgWinRate");
		float avgLossRate = (Float) simulateResult.get("avgLossRate");
		
		List<Trade> trades = (List<Trade>) simulateResult.get("trades");
		
		
		List<Profit> profits = (List<Profit>) simulateResult.get("profits");
		List<AnnualProfit> annualProfits = (List<AnnualProfit>) simulateResult.get("annualProfits");
		List<IndexData> indexDatas = allIndexDatas;
		Map<String,Object> result = new HashMap<>();
		

		

		float years = indexDataService.getYear(allIndexDatas);
		
		float indexIncomeTotal = (indexDatas.get(indexDatas.size()-1).getClosePoint() - indexDatas.get(0).getClosePoint()) / indexDatas.get(0).getClosePoint();
		float indexIncomeAnnual = (float) Math.pow(1+indexIncomeTotal, 1/years) - 1;
		float trendIncomeTotal = (profits.get(profits.size()-1).getValue() - profits.get(0).getValue()) / profits.get(0).getValue();
		float trendIncomeAnnual = (float) Math.pow(1+trendIncomeTotal, 1/years) - 1;
//		trendIncome
		
		result.put("indexDatas", indexDatas);
		
		result.put("startDate", startDate);
		result.put("endDate", endDate);
		
		result.put("profits", profits);		
		result.put("annualProfits", annualProfits);		
		result.put("indexIncomeTotal", indexIncomeTotal);		
		result.put("indexIncomeAnnual", indexIncomeAnnual);		
		result.put("trendIncomeTotal", trendIncomeTotal);		
		result.put("trendIncomeAnnual", trendIncomeAnnual);		
		result.put("years", years);		
		
		
		result.put("winCount", winCount);
		result.put("lossCount", lossCount);
		result.put("avgWinRate", avgWinRate);
		result.put("avgLossRate", avgLossRate);
		result.put("trades", trades);
		
		return result;
	}

	private List<IndexData> filterByDateRange(List<IndexData> allIndexDatas, String strStartDate, String strEndDate) {
		if(StrUtil.isBlankOrUndefined(strStartDate) || StrUtil.isBlankOrUndefined(strEndDate) )
			return allIndexDatas;
		
		List<IndexData> result = new ArrayList<>();
		Date startDate = DateUtil.parse(strStartDate);
		Date endDate = DateUtil.parse(strEndDate);
		

		for (IndexData indexData : allIndexDatas) {
			
			Date date =DateUtil.parse(indexData.getDate());
			
			if(
					date.getTime()>=startDate.getTime() &&
					date.getTime()<=endDate.getTime() 
					) {
				
				result.add(indexData);
				
				
			}
				
			
			
		}
		
		
		
		
		
		return result;
	}
	
	
	
	
}

