package cn.how2j.trend.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import cn.how2j.trend.pojo.IndexData;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;

@Service
public class IndexDataService {
	
	@Autowired RestTemplate restTemplate;
	

	public List<IndexData> listIndexData(String code){
		List<Map> temp= restTemplate.getForObject("http://127.0.0.1:9888/trend/indices/"+code+".json",List.class);
		List<IndexData> indexDatas =map2IndexData(temp);
		Collections.reverse(indexDatas);
		return indexDatas;
	}

	private List<IndexData> map2IndexData(List<Map> temp) {
		List<IndexData> indexDatas = new ArrayList<>();
		for (Map map : temp) {
			String date = map.get("date").toString();
			float closePoint = Convert.toFloat(map.get("closePoint"));
			IndexData indexData = new IndexData();
			
			indexData.setDate(date);
			indexData.setClosePoint(closePoint);
			indexDatas.add(indexData);
		}
		
		return indexDatas;
	}
	public float getYear(List<IndexData> allIndexDatas) {
			float years;
			String sDateStart = allIndexDatas.get(0).getDate();
			String sDateEnd = allIndexDatas.get(allIndexDatas.size()-1).getDate();
			
			Date dateStart = DateUtil.parse(sDateStart);
			Date dateEnd = DateUtil.parse(sDateEnd);
			
			long days = DateUtil.between(dateStart, dateEnd, DateUnit.DAY);
			years = days/365f;
			return years;
	}
}
