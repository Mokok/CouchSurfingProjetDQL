
<!DOCTYPE html>
<html>
	<%@ include file="entete.jsp" %>
	<%@ include file="menu.jsp" %>
    <body style="background-image:url(${pageContext.request.contextPath}/ServletBackground)">
        <div id="container">
	        <form id="form_criteres" method="post" action="criteres">
	    		<h5>Crit�res !</h5>
	    		<div id="dateDispo">
		    		<label>Date de d�but </label><input class="date" type="date" name="dateDebut">
		    		<label>Date de fin </label><input class="date" type="date" name="dateFin">
	    		</div>
	    		<div id="services1">
	    			<ul>
		    			<li> 
		    				<label for="crCommerce" class="icon-basket-1 crCommerce criteresjsp_service"></label>
		    				<input placeholder="Commerces" type="text" name="crCommerce" id="crCommerce"/>
		    			</li>
						<li>
							<label for="crHopitaux" class="icon-h-sigh criteresjsp_service"></label>
							<input placeholder="Soins" type="text" name="crHopitaux" id="crHopitaux"/>
						</li>
						<li>
							<label for="crRestaurants" class="icon-restaurant criteresjsp_service"></label>
							<input placeholder="Restaurants" type="text" name="crRestaurants" id="crRestaurants"/>
						</li>
						<li>
							<label for="crTransports" class="icon-bus criteresjsp_service"></label>
							<input placeholder="Transports" type="text" name="crTransports" id="crTransports"/>
						</li>
						<li>
							<label for="crAnimaux" class="icon-paw criteresjsp_service"></label>
							<input placeholder="Animaux" type="text" name="crAnimaux" id="crAnimaux"/>
						</li>
							
	    			</ul>
	    			
	    		</div>
	    		<div id="services2">
	    			<ul>
	    				<li>
							<label for="crInternet" class="icon-signal criteresjsp_service"></label>
							<input placeholder="Internet" type="text" name="crInternet" id="crInternet"/>
						</li>
						<li> 
							<label for="crHandicapes" class="icon-wheelchair criteresjsp_service"></label>
							<input placeholder="Handicapes" type="text" name="crHandicapes" id="crHandicapes"/>
						</li>
						<li>
							<label for="crFumeurs" class="icon-fire criteresjsp_service"></label>
							<input placeholder="Fumeurs" type="text" name="crFumeur" id="crFumeurs"/>
						</li>
						<li>
							<label for="crParking" class="icon-cab criteresjsp_service"></label>
							<input placeholder="Parking" type="text" name="crParking" id="crParking"/>
						</li>
							
	    			</ul>
	    			
	    		</div>
	    		<input type="Submit" value="OK"></input>
			</form>
        </div>
    </body>
	<%@ include file="basdepage.jsp" %>

