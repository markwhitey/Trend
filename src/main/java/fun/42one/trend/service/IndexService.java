package cn.how2j.trend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import cn.how2j.trend.pojo.Index;

@Service
public class IndexService {
	
	@Autowired RestTemplate restTemplate;
	
	public List<Index> listIndex(){
		List<Index> indexs= restTemplate.getForObject("http://127.0.0.1:9888/trend/indices/codes.json",List.class);
		return indexs;
	}
	
	
	
	
	
	
	
	
}
