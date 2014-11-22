package com.sungale.dropship2.process.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sungale.dropship2.model.ShopHqOrder;
import com.sungale.dropship2.model.UpsCsv;

public class ShopHqOrderTool {

	public static String getOrderFileName(String orderFilePath){
		if(orderFilePath.endsWith(".xml"))
			return orderFilePath.substring(orderFilePath.lastIndexOf(System.getProperty("file.separator"))+1, orderFilePath.lastIndexOf(".xml"));
		else
			return orderFilePath.substring(orderFilePath.lastIndexOf(System.getProperty("file.separator"))+1);
	}
	
	
	public static List<ShopHqOrder> getShopHqOrderList(String orderFilePath) throws Exception{
		List<ShopHqOrder> rtn = new ArrayList<ShopHqOrder>();
		File fXmlFile = new File(orderFilePath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
	 
		doc.getDocumentElement().normalize();
	 
//		System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
	 
		NodeList nList = doc.getElementsByTagName("hubOrder");
//		System.out.println("----------------------------");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			 
			Node nNode = nList.item(temp);
	 
//			System.out.println("\nCurrent Element :" + nNode.getNodeName());
	 
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	 
				Element eElement = (Element) nNode;

				Element shipTo = (Element)eElement.getElementsByTagName("shipTo").item(0);
				String shipToPersonPlaceID  = shipTo.getAttribute("personPlaceID");
				Element personPlace = (Element)eElement.getElementsByTagName("personPlace").item(0);
				
				ShopHqOrder order = new ShopHqOrder();
				if(!personPlace.getAttribute("personPlaceID").equals(shipToPersonPlaceID)){
					personPlace = (Element)eElement.getElementsByTagName("personPlace").item(1);
				}
				
				order.setPersonName(personPlace.getElementsByTagName("name1").item(0).getTextContent());
				order.setAddress(personPlace.getElementsByTagName("address1").item(0).getTextContent());
				if(personPlace.getElementsByTagName("address2").item(0) != null)
					order.setAddress2(personPlace.getElementsByTagName("address2").item(0).getTextContent());
				order.setCity(personPlace.getElementsByTagName("city").item(0).getTextContent());
				order.setState(personPlace.getElementsByTagName("state").item(0).getTextContent());
				order.setCountry(personPlace.getElementsByTagName("country").item(0).getTextContent());
				order.setZip(personPlace.getElementsByTagName("postalCode").item(0).getTextContent());
				order.setPhone(personPlace.getElementsByTagName("dayPhone").item(0).getTextContent());
				
				order.setShippingCode(eElement.getElementsByTagName("shippingCode").item(0).getTextContent());
				
				order.setCustomOrderNumber(eElement.getElementsByTagName("custOrderNumber").item(0).getTextContent());
				order.setQtyOrdered(Integer.parseInt(eElement.getElementsByTagName("qtyOrdered").item(0).getTextContent()));
				order.setMerchantLineNumber(eElement.getElementsByTagName("merchantLineNumber").item(0).getTextContent());
				order.setMerchantSku(eElement.getElementsByTagName("merchantSKU").item(0).getTextContent());
				order.setTrxDate(eElement.getElementsByTagName("orderDate").item(0).getTextContent());
				order.setTrxId(eElement.getElementsByTagName("orderId").item(0).getTextContent());
				order.setPoNumber(eElement.getElementsByTagName("poNumber").item(0).getTextContent());

				order.setUnitCost(Double.parseDouble(eElement.getElementsByTagName("unitCost").item(0).getTextContent()));
				order.setVendorSku(eElement.getElementsByTagName("vendorSKU").item(0).getTextContent());
				
				rtn.add(order);
			}
		}
		return rtn;
		
	}
	
	public static UpsCsv transformShopHqOrderItemToUpsCsvItem(ShopHqOrder order){
		UpsCsv ups = new UpsCsv();
		ups.setShipTo_CompanyOrName(order.getPersonName());
		ups.setShipTo_StreetAddress(order.getAddress());
		ups.setShipTo_RoomFloorAddress2(order.getAddress2());
		ups.setShipTo_City(order.getCity());
		ups.setShipTo_State(order.getState());
		ups.setShipTo_Country(order.getCountry());
		ups.setShipTo_ZipCode(order.getZip());
		ups.setShipTo_Telephone(order.getPhone());
		ups.setPackage_Reference1(ShopHqOrderTool.makePackageReference1(order));
		ups.setPackage_Reference2(ShopHqOrderTool.makePackageReference2(order));
		//Constants
		ups.setPackage_PackageType("Package");
		switch(order.getShippingCode()){
		case "UNSP_CG": case "UPSN_CG": case "UG": case "UPSG":  	
			ups.setShipmentInformation_ServiceType("Ground");
			break;
		case "UPSN_SE": case "UB": case "UPSN_SC":
			ups.setShipmentInformation_ServiceType("UPS 2nd Day Air");
			break;
		case "UPSN_3D": case "UPS3":
			ups.setShipmentInformation_ServiceType("UPS 3 Day Select");
			break;
		case "UPSET_ND": case "UZ": 
			ups.setShipmentInformation_ServiceType("UPS Next Day Air, Signature Required");
			break;
		default:
			ups.setShipmentInformation_ServiceType("Ground");
			break;
		}
		ups.setShipTo_ResidentialIndicator("y");
		
		//set thirdparty shipping details
		ups.setShipmentInformation_BillingOption("TP");
		ups.setThirdParty_CompanyOrName("RSPA, INC.(ShopHQ fulfillment)");
		ups.setThirdParty_Address("13941 CENTRAL AVE");
		ups.setThirdParty_Country("United States");
		ups.setThirdParty_PostalCode("91710");
		ups.setThirdParty_City("Chino");
		ups.setThirdParty_State("CA");
		ups.setThirdParty_Telephone("9099021807");
		ups.setThirdParty_UPSAccount("W73528");
		return ups;
	}
	
	public static  String makePackageReference1(ShopHqOrder order){
		return "Order# "+order.getCustomOrderNumber();
	}
	public static String makePackageReference2(ShopHqOrder order){
		return "PO# "+order.getPoNumber()+", "+order.getQtyOrdered()+"pcs "+order.getVendorSku();
	}
}
