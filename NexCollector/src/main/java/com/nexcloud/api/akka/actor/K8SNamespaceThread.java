package com.nexcloud.api.akka.actor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import com.nexcloud.fullfillment.k8s.domain.Item;
import com.nexcloud.util.SendDataLoader;
import com.nexcloud.util.Util;

@Configuration
@EnableAutoConfiguration
public class K8SNamespaceThread extends Thread {

	static final Logger 	logger 				= LoggerFactory.getLogger(K8SNamespaceThread.class);
	
	private String msg							= null;
	
	private List<Item> items					= null;
	
	
	private List<Map<String,Object>> list		= null;
	
	
	private static K8SNamespaceThread thisObj 	= null;

	
	public synchronized static K8SNamespaceThread getInstance(){
		if ( thisObj == null ){
			//System.out.println("ConfigLoader getInstance 실행!!!!!!!!!!!!!!!");
			try {
				thisObj = new K8SNamespaceThread();
			}catch(IndexOutOfBoundsException ie){
				System.out.println("K8SNamespaceThread Class getInstance IndexOutOfBoundsException Error = " +ie);
			}catch(NullPointerException ne){
				System.out.println("K8SNamespaceThread Class getInstance NullPointerException Error = " +ne);
			} catch(Exception e) {
				System.out.println("K8SNamespaceThread Class getInstance Exception Error = " + e);
			}	
		}	
		
		return thisObj;
	}
	
	public K8SNamespaceThread(){ 
		list				= new ArrayList<Map<String,Object>>();
		start();
	}
	
	
	/**
	 * Resource Data Set
	 * @param key
	 * @param object
	 */
	public synchronized void set( List<Item> items )
	{
		Map<String,Object> data	= new HashMap<String, Object>();
		try{
			data.put("items", items);
			
			list.add(data);
			//inputdata++;
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Resource Data Get
	 * @param key
	 * @return
	 */
	public synchronized Map<String,Object> get( )
	{
		try{
			if( list.size() > 0)
			{
				return list.remove(0);
			}
			else
				return null;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	
	public void run()
	{
		String pattern 						= "#####.###";
        DecimalFormat dformat 				= new DecimalFormat( pattern );
        
		//logger.error("["+Thread.currentThread()+"]Host Start!!");
        Map<String,Object> data				= null;
        
        while(true)
        {
			try
			{
				data						= get();
				if( data != null )
				{
					items					= (List<Item>)data.get("items");
					
					msg						= "";
					
					for( Item item : items )
					{
						msg 							+= "k8s_namespace,namespace="+item.getMetadata().getName();
						int active						= 0;
						int terminating					= 0;
						
						if( "Active".equals(item.getStatus().getPhase()) )
							active						= 1;
						
						if( "Terminating".equals(item.getStatus().getPhase()) )
							terminating					= 1;
						
						msg 							+= " active="+active+",terminating="+terminating+"\n";
					}
					
					if( msg != null && !"".equals(msg.trim()) )
						this.send(msg);
				}
				else
					Thread.sleep(10);
	
			}catch(Exception e){
				e.printStackTrace();
			}
        }
		//logger.error("["+Thread.currentThread()+"]Host End!!");
	}
	
	/**
	 * Influx DB Write
	 * @param influxDB
	 * @param msg
	 */
	public void send( String msg )
	{
		try {
			//HttpAPI.request(influxdb_endpoint+"/write?consistency=any&db="+influxdb_datasource+"&precision=ns", MediaType.TEXT_PLAIN_TYPE, Method.POST, msg);
			//logger.error("Host data Send!!!\n"+msg);
			
			SendDataLoader.getInstance().set(msg);
		} catch (Exception e) {
			System.out.println("influx send data :: " + msg );
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(Util.makeStackTrace(e));
		}
	}
}