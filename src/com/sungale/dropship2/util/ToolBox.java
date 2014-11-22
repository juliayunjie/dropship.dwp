package com.sungale.dropship2.util;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

public class ToolBox {
	
	public static String getToday(){
		Date date = new Date();
		String modifiedDate= new SimpleDateFormat("yyyyMMdd").format(date);
		return modifiedDate;
	}

	public static String convertMultipartFileToFile(MultipartFile mpf) throws Exception{
		File rtn = new File(Constants.tempFilePath+mpf.getOriginalFilename());
		rtn.createNewFile();
		FileOutputStream fos = new FileOutputStream(rtn);
		fos.write(mpf.getBytes());
		fos.close();
		return Constants.tempFilePath+mpf.getOriginalFilename();
	}
	
}
