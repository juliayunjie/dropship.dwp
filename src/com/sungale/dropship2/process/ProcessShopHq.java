/**
 * 
 */
package com.sungale.dropship2.process;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.web.multipart.MultipartFile;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sungale.dropship2.model.UpsShippingCsv;
import com.sungale.dropship2.util.ToolBox;
import com.sungale.dropship2.util.WriteUpsCsv;
import com.sungale.dropship2.util.Constants;
import com.sungale.dropship2.model.UpsCsv;
import com.sungale.dropship2.model.ShopHqOrder;
import com.sungale.dropship2.process.tool.ShopHqOrderTool;
import com.sungale.dropship2.process.tool.UpsExportCsvTool;

/**
 * @author Julia Sun
 *
 */
public class ProcessShopHq {
	
	public String generateUpsShippingImportCsv(List<MultipartFile> files){
		String rtn="";
		try{
			
			//1. get ShopHq orders
			List<ShopHqOrder> orderList = new ArrayList<ShopHqOrder>();
			for(MultipartFile orderFile: files){
				orderList.addAll(ShopHqOrderTool.getShopHqOrderList(ToolBox.convertMultipartFileToFile(orderFile)));
			}
			//2. transform into Ups Csv items
			List<UpsCsv> upsCsvList = new ArrayList<UpsCsv>();
			for(ShopHqOrder order: orderList){
				upsCsvList.add(ShopHqOrderTool.transformShopHqOrderItemToUpsCsvItem(order));
			}
			//3. generate and write the csv file
			WriteUpsCsv writer = new WriteUpsCsv();
			String fileName = "order_combined_ups_"+ToolBox.getToday()+".csv";				
			writer.writeWithCsvBeanWriter(upsCsvList, Constants.OUTPUTPATH+fileName);
			rtn = "Sucessfully generated ups shipping import csv at: "+Constants.OUTPUTURL+fileName;		
			
			
		}catch(Exception e){
			e.printStackTrace();
			rtn = "faile to generate ups shipping import csv file: " + e.getLocalizedMessage();
		}
		return rtn;
	}
	
	public String generateShppingXml(String orderFilePath,String upsShipmentFilePath){
		String rtn = "";
		try{
			//1. prepare xml document
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
		// 2. create root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("ConfirmMessageBatch");
		doc.appendChild(rootElement);
 
		Element partner= doc.createElement("partnerID");
		rootElement.appendChild(partner);
		partner.setAttribute("name", "RSPA, Inc. (Sungale)");
		partner.setAttribute("roleType", "vendor");
		partner.appendChild(doc.createTextNode("rspainc"));
 
		//3. write shipping elements based on order and ups shipping
		List<ShopHqOrder> orders = ShopHqOrderTool.getShopHqOrderList(orderFilePath);
		int count = 1;
		for(ShopHqOrder order: orders){
			UpsShippingCsv ups = UpsExportCsvTool.findUpsShipping(order, upsShipmentFilePath); 
//System.out.println(ups);
			doc = this.writeSingleConfirm(doc, rootElement, order, ups, count);
			count++;
		}
		
		// 4. finish root elements
		Element messageCount = doc.createElement("messageCount");
		messageCount.appendChild(doc.createTextNode(""+orders.size()));		
		rootElement.appendChild(messageCount);
		
		// 5. write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		String fileName = ShopHqOrderTool.getOrderFileName(orderFilePath)+"_shipment.xml";
		StreamResult result = new StreamResult(new File(Constants.OUTPUTPATH+fileName));
 
		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);
 
		transformer.transform(source, result);
 
		rtn = "sucessfully generated shipping xml at ::"+Constants.OUTPUTURL+fileName;
 
	  } catch (Exception pce) {
		  pce.printStackTrace();
		  rtn = "fail to generate shipping xml for "+orderFilePath + pce.getLocalizedMessage();
	  } 
		return rtn;
	}
	
	private Document writeSingleConfirm(Document doc, Element rootElement, ShopHqOrder order, UpsShippingCsv ups, int count){
		Element hubConfirm = doc.createElement("hubConfirm");
		rootElement.appendChild(hubConfirm);
		
		//participatingParty elements
		Element participatingParty = doc.createElement("participatingParty");
		hubConfirm.appendChild(participatingParty);
		participatingParty.setAttribute("name", "shopHQ");
		participatingParty.setAttribute("participationCode", "To:");
		participatingParty.setAttribute("roleType", "merchant");
		participatingParty.appendChild(doc.createTextNode("snbc"));
		
		//partnerTrxID elements
		Element partnerTrxID = doc.createElement("partnerTrxID");
		partnerTrxID.appendChild(doc.createTextNode(order.getTrxId()));
		hubConfirm.appendChild(partnerTrxID);
		
		//partnerTrxDate elements
		Element partnerTrxDate = doc.createElement("partnerTrxDate");
		partnerTrxDate.appendChild(doc.createTextNode(order.getTrxDate()));
		hubConfirm.appendChild(partnerTrxDate);
		
		//poNumber elements
		Element poNumber = doc.createElement("poNumber");
		poNumber.appendChild(doc.createTextNode(order.getPoNumber()));
		hubConfirm.appendChild(poNumber);
		
		//hubAction elements
		Element hubAction = doc.createElement("hubAction");
		hubConfirm.appendChild(hubAction);
		
		//action elements
		Element action = doc.createElement("action");
		action.appendChild(doc.createTextNode("v_ship"));
		hubAction.appendChild(action);
		
		//merchantLineNumber elements
		Element merchantLineNumber  = doc.createElement("merchantLineNumber");
		merchantLineNumber.appendChild(doc.createTextNode(order.getMerchantLineNumber()));
		hubAction.appendChild(merchantLineNumber);
		
		//trxVendorSKU elements
		Element trxVendorSKU = doc.createElement("trxVendorSKU");
		trxVendorSKU.appendChild(doc.createTextNode(order.getVendorSku()));
		hubAction.appendChild(trxVendorSKU);
		
		//trxMerchantSKU elements
		Element trxMerchantSKU = doc.createElement("trxMerchantSKU");
		trxMerchantSKU.appendChild(doc.createTextNode(order.getMerchantSku()));
		hubAction.appendChild(trxMerchantSKU);
		
		//trxQty elements
		Element trxQty = doc.createElement("trxQty");
		trxQty.appendChild(doc.createTextNode(order.getQtyOrdered().toString()));
		hubAction.appendChild(trxQty);
		
		String packageDetailID = String.format("%03d", count);
		
		//packageDetailLink elements
		Element packageDetailLink = doc.createElement("packageDetailLink");
		packageDetailLink.setAttribute("packageDetailID", "P_"+packageDetailID);
		hubAction.appendChild(packageDetailLink);
		
		//packageDetail elements
		Element packageDetail = doc.createElement("packageDetail");
		packageDetail.setAttribute("packageDetailID", "P_"+packageDetailID);
		hubConfirm.appendChild(packageDetail);
		
		//shipDate elements
		Element shipDate = doc.createElement("shipDate");
		shipDate.appendChild(doc.createTextNode(ToolBox.getToday()));
		packageDetail.appendChild(shipDate);
		
		//serviceLevel1 elements
		Element serviceLevel1 = doc.createElement("serviceLevel1");
		switch (order.getShippingCode()){
			case "UNSP_CG": case "UPSN_CG": case "UG": case "UPSG":  	
				serviceLevel1.appendChild(doc.createTextNode("UPSN_CG"));
				break;
			case "UPSN_SE": case "UB": case "UPSN_SC":
				serviceLevel1.appendChild(doc.createTextNode("UPSN_SC"));
				break;
			case "UPSN_3D": case "UPS3":
				serviceLevel1.appendChild(doc.createTextNode("UPSN_3D"));
				break;
			case "UPSET_ND": case "UZ": 
				serviceLevel1.appendChild(doc.createTextNode("UPSN_ND"));
				break;
			default:
				serviceLevel1.appendChild(doc.createTextNode("UPSN_CG"));
				break;	
		}
//		serviceLevel1.appendChild(doc.createTextNode("UPSN_CG"));
		packageDetail.appendChild(serviceLevel1);
		
		//trackingNumber elements
		Element trackingNumber = doc.createElement("trackingNumber");
		trackingNumber.appendChild(doc.createTextNode(ups.getPackageTrackingNumber()));
		packageDetail.appendChild(trackingNumber);
		
		return doc;
	}

	public String generateInvoiceXml(String orderFilePath, String upsShipmentFilePath){
		String rtn=""; 
		try {
			//1. preparte xml document
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	 
			//2. create root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("InvoiceMessageBatch");
			doc.appendChild(rootElement);
	 
			Element partner= doc.createElement("partnerID");
			rootElement.appendChild(partner);
			partner.setAttribute("name", "RSPA, Inc. (Sungale)");
			partner.setAttribute("roleType", "vendor");
			partner.appendChild(doc.createTextNode("rspainc"));
	 
			//3. write invoice elements
			List<ShopHqOrder> orders = ShopHqOrderTool.getShopHqOrderList(orderFilePath);
			for(ShopHqOrder order: orders){
				UpsShippingCsv ups = UpsExportCsvTool.findUpsShipping(order, upsShipmentFilePath); 
//System.out.println(ups);
				doc = this.writeSingleInvoice(doc, rootElement, order, ups);
			}
			
			//4. finish root elements
			Element messageCount = doc.createElement("messageCount");
			messageCount.appendChild(doc.createTextNode(""+orders.size()));		
			rootElement.appendChild(messageCount);
			
			//5. write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
//			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			DOMSource source = new DOMSource(doc);
			String fileName = ShopHqOrderTool.getOrderFileName(orderFilePath)+"_invoice.xml";
			StreamResult result = new StreamResult(new File(Constants.OUTPUTPATH+fileName));
	 
			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
	 
			transformer.transform(source, result);
	 
			rtn = "successfully generated invoice ::"+Constants.OUTPUTURL+fileName;
	 
		  } catch (Exception pce) {
			pce.printStackTrace();
			rtn = "fail to generate invoice xml for "+orderFilePath + pce.getLocalizedMessage();
		  } 
		return rtn;
	}
	
	private Document writeSingleInvoice(Document doc, Element rootElement, ShopHqOrder shopHq, UpsShippingCsv ups){
		// hubInvoice elements
					Element hubInvoice = doc.createElement("hubInvoice");
					rootElement.appendChild(hubInvoice);
					
					//participatingParty elements
					Element participatingParty = doc.createElement("participatingParty");
					hubInvoice.appendChild(participatingParty);
					participatingParty.setAttribute("name", "shopHQ");
					participatingParty.setAttribute("participationCode", "To:");
					participatingParty.setAttribute("roleType", "merchant");
					participatingParty.appendChild(doc.createTextNode("snbc"));
					
					//partnerTrxId elements
					Element partnerTrxId = doc.createElement("partnerTrxID");
					hubInvoice.appendChild(partnerTrxId);
					partnerTrxId.appendChild(doc.createTextNode(shopHq.getTrxId()));	
					
					// partnerTrxDate elements
					Element partnerTrxDate = doc.createElement("partnerTrxDate");
					partnerTrxDate.appendChild(doc.createTextNode(shopHq.getTrxDate()));	
					hubInvoice.appendChild(partnerTrxDate);
			 
					// poNumber elements
					Element poNumber = doc.createElement("poNumber");
					poNumber.appendChild(doc.createTextNode(shopHq.getPoNumber()));	
					hubInvoice.appendChild(poNumber);
			 
					// trxShipping elements
					Element trxShipping = doc.createElement("trxShipping");
					double shippingCost = 0.00; 
					if(ups!=null && ups.getShipmentInformationBillingOption().trim().equals("Bill Shipper Freight"))
						shippingCost = Double.parseDouble(ups.getPackagePackagePublishedCharge());
					trxShipping.appendChild(doc.createTextNode(shippingCost+""));	
					hubInvoice.appendChild(trxShipping);
					
					//trxBalanceDue elements
					Element trxBalanceDue = doc.createElement("trxBalanceDue");
					hubInvoice.appendChild(trxBalanceDue);
					Double balanceDue = shippingCost +shopHq.getUnitCost();
					balanceDue = Math.round(balanceDue*100.0)/100.0;
					trxBalanceDue.appendChild(doc.createTextNode(balanceDue.toString()));	 
					
					// hubAction elements
					Element hubAction = doc.createElement("hubAction");
					hubInvoice.appendChild(hubAction);
			 
					// action elements
					Element action = doc.createElement("action");
					action.appendChild(doc.createTextNode("v_invoice"));	
					hubAction.appendChild(action);
			 
					// merchantLineNumber elements
					Element merchantLineNumber = doc.createElement("merchantLineNumber");
					merchantLineNumber.appendChild(doc.createTextNode(shopHq.getMerchantLineNumber()));	
					hubInvoice.appendChild(merchantLineNumber);
			 
					//trxVendorSKU elements
					Element trxVendorSKU = doc.createElement("trxVendorSKU");
					hubAction.appendChild(trxVendorSKU);
					trxVendorSKU.appendChild(doc.createTextNode(shopHq.getVendorSku()));	 
					
					// trxMerchantSKU elements
					Element trxMerchantSKU = doc.createElement("trxMerchantSKU");
					hubAction.appendChild(trxMerchantSKU);
					trxMerchantSKU.appendChild(doc.createTextNode(shopHq.getMerchantSku())); 
			 
					// trxQty elements
					Element trxQty = doc.createElement("trxQty");
					trxQty.appendChild(doc.createTextNode(shopHq.getQtyOrdered().toString()));		
					hubAction.appendChild(trxQty);
			 
					// trxUnitCost elements
					Element trxUnitCost = doc.createElement("trxUnitCost");
					trxUnitCost.appendChild(doc.createTextNode(shopHq.getUnitCost().toString()));	
					hubAction.appendChild(trxUnitCost);
					
					// merchantLineNumber elements
					Element trxLineShipping = doc.createElement("trxLineShipping");
					trxLineShipping.appendChild(doc.createTextNode("0.00"));	
					hubAction.appendChild(trxLineShipping);
			 
					//trxLineTax elements
					Element trxLineTax = doc.createElement("trxLineTax");
					hubAction.appendChild(trxLineTax);
					trxLineTax.appendChild(doc.createTextNode("0.00"));	 
					
					// trxLineMiscCharges elements
					Element trxLineMiscCharges = doc.createElement("trxLineMiscCharges");
					hubAction.appendChild(trxLineMiscCharges);
					trxLineMiscCharges.appendChild(doc.createTextNode("0.00")); 
			 
					
			return doc;
	}
}
