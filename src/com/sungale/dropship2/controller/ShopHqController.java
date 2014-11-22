/**
 * 
 */
package com.sungale.dropship2.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.sungale.dropship2.process.ProcessShopHq;
import com.sungale.dropship2.util.ToolBox;

/**
 * @author Julia Sun
 *
 */
@Controller
@RequestMapping("/shophq")
public class ShopHqController extends HttpServlet{
	private ProcessShopHq tool = new ProcessShopHq();
	
	@ResponseBody
	@RequestMapping(value = "process.do", method = RequestMethod.POST)
	public void process(@RequestParam("file")List<MultipartFile> files, @RequestParam("operation") String operation, 
			@RequestParam("uspFile") MultipartFile upsFile, HttpServletResponse response){
		try{
		ServletOutputStream out = response.getOutputStream();
System.out.println(files.size()+operation+upsFile.getName());
		if(files.size()==0){
			out.print("no order file selected, go back and retry");
		}
		String result = "";
		switch(operation){
				case "genShippingCsv":{
					result = tool.generateUpsShippingImportCsv(files);
					break;
				}
				case "genShippingXml":{
					for(MultipartFile orderFile: files){
						result += tool.generateShppingXml(ToolBox.convertMultipartFileToFile(orderFile), ToolBox.convertMultipartFileToFile(upsFile))+"\n";
					}
					break;
				}
				case "genInvoiceXml":{
					for(MultipartFile orderFile: files){
						result += tool.generateInvoiceXml(ToolBox.convertMultipartFileToFile(orderFile), ToolBox.convertMultipartFileToFile(upsFile))+"\n";
					}
					break;
				}
			}
			out.print(result);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value= "index.htm", method = RequestMethod.GET)
	public String show(ModelMap model){
		model.addAttribute("message", "Welcome to ShopHq dashbord!");
		return "main";
	}
}
