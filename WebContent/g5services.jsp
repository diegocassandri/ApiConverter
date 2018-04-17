<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    	               "http://www.w3.org/TR/html4/loose.dtd">

<html>
  <head>
    	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    	<title>GlassFish JSP Page</title>
    	<script  src="https://code.jquery.com/jquery-2.2.4.min.js"  integrity="sha256-BbhdlvQf/xTY9gja0Dq3HiwQF8LaCRTXxZKRutelT44="  crossorigin="anonymous"></script>
    	<script> 
    	var data = null;
    	$.ajax({
    	    url: 'G5Rest',
    	    type: 'post',
    	    dataType: 'json',
    	    headers: {    	        
    	    	user: 'senior', 
    	        pass: 'senior', 
    	        encryptionType: '0' //(0-texto plano ou 2-token > default 2)
    	    },
    	    data: {
    	    	//parametros obrigatórios
    	    	server: 'http://teste95:8080', 
    	    	module: 'rubi', 
    	    	service: 'com.senior._wf.utils',
    	    	port: 'getColaborador',
    	    	//parametros opcionais
    	    	numEmp: '1', 
    	    	tipCol: '1', 
    	    	numCad: '1', 
    	    	returnJSON: '1'
    	    },
    	    success: function (data) {    	        
    	        window.data = data;
    	        $("#rest").html(JSON.stringify(data));
    	    }
    	});
    	
    	var envelope = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.senior.com.br\"><SOAP-ENV:Header/><SOAP-ENV:Body><ser:getColaborador><user>senior</user><password>senior</password><encryption>0</encryption><pass>senior</pass><encrypted>0</encrypted><parameters><searchTerm>marc</searchTerm></parameters></ser:getColaborador></SOAP-ENV:Body></SOAP-ENV:Envelope>";
    	$.ajax({
    	    url: 'G5Soap',
    	    type: 'post',
    	    dataType: 'xml',
    	    headers: {    	        
    	    	user: 'senior', 
    	        pass: 'senior', 
    	        encryptionType: '0' //(0-texto plano ou 2-token > default 2)
    	    },
    	    data: envelope,
    	    success: function (data) {    	        
    	        window.data = data;
    	        $("#soap").html(data);
    	    },
    	    error: function(data,e,b){
    	    	debugger;
    	    	
    	    }
    	});
    	</script>
  </head>
  <body>
  	Rest:<br>
    <div id="rest">Aguarde a requisição rest</div>
    Soap:<br>
    <div id="soap">Aguarde a requisição soap</div>    
  </body>
</html> 
