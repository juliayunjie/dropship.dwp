<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>ShopHq Dashboard</title>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
<script>
$("input:checkbox").on('click', function() {
	  // in the handler, 'this' refers to the box clicked on
	  var $box = $(this);
	  if ($box.is(":checked")) {
	    // the name of the box is retrieved using the .attr() method
	    // as it is assumed and expected to be immutable
	    var group = "input:checkbox[name='" + $box.attr("name") + "']";
	    // the checked state of the group/box on the other hand will change
	    // and the current value is retrieved using .prop() method
	    $(group).prop("checked", false);
	    $box.prop("checked", true);
	  } else {
	    $box.prop("checked", false);
	  }
	});
</script>
</head>
<body>
<h2>${message}</h2>
	<form id="upload" action="./process.do" enctype="multipart/form-data" method="post" >
	<label>Choose what to do (choose only one): </label>
	<select id="operation" name="operation">
		<option value="genShippingCsv">Generate UPS Shipping Import CSV</option>
		<option value="genShippingXml">Generate Shipping Xml</option>
		<option value="genInvoiceXml">Generate Invoice Xml</option>
	</select><br/>
	
	<label>Choose original orders (allow multiple): </label><input type="file" name="file" id="file" multiple><br/>
	<label>Choose UPS export file (choose only one): </label><input type="file" name="uspFile" id="upsFile"><br/>
	<input type="submit"  value="submit"/>
	</form>
</body>
</html>