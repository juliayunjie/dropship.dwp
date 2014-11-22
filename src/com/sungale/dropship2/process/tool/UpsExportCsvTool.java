package com.sungale.dropship2.process.tool;

import java.io.FileReader;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import com.sungale.dropship2.model.ShopHqOrder;
import com.sungale.dropship2.model.UpsShippingCsv;

public class UpsExportCsvTool {

	public static CellProcessor[] getProcessors() {
		final CellProcessor[] processors = new CellProcessor[] { 
  									new Optional(), 
  									new Optional(),	
  									new Optional(), 
  									new Optional(),	
  									new Optional(),	
  									new Optional(),	
  									new Optional(), 
  						  			new Optional(), 
  						  			new Optional(), 
  						  			new Optional(), 
  						  			new Optional(), 	
  						  			new Optional(),	
  						  			new Optional(),	
  						  			new Optional(),	
  						  			new Optional(),	
  						  			new Optional(),
  						  			new Optional(),	
  						  			new Optional()	
  						  				
  						  		};
  		
  		return processors;
  	}
	
	public static UpsShippingCsv findUpsShipping(ShopHqOrder order, String upsShipmentFilePath) throws Exception{
		UpsShippingCsv rtn = new UpsShippingCsv();
		ICsvBeanReader beanReader = null;
  		try {
  			beanReader = new CsvBeanReader(new FileReader(upsShipmentFilePath), CsvPreference.STANDARD_PREFERENCE);
  			
  			final String[] header = beanReader.getHeader(true);
  			final CellProcessor[] processors = getProcessors();
  			
  			UpsShippingCsv ups;
			String match1 = ShopHqOrderTool.makePackageReference1(order);
			String match2 = ShopHqOrderTool.makePackageReference2(order);
//System.out.println(match1+"---"+match2);			
  			while( (ups = beanReader.read(UpsShippingCsv.class, header, processors)) != null ) {
//  				System.out.println(String.format("lineNo=%s, rowNo=%s, dropship=%s", beanReader.getLineNumber(),beanReader.getRowNumber(), ups));
  				
  				if(ups.getPackageReference1().trim().equals(match1) && ups.getPackageReference2().trim().equals(match2)){
  					rtn = ups;
  					break;
  				}
 			}
 		}
 		finally {
 			if( beanReader != null ) {
 				beanReader.close();
 			}
 		}
		
		
		return rtn;
	}
}
