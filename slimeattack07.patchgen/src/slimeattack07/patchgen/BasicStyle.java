package slimeattack07.patchgen;

public class BasicStyle {
	
	public static String getStyle() {
		return """
#body{
	background-color: rgb(128, 128, 128);
}
#header{
	background-color: rgb(102, 102, 102);
	color: #000000;
}
#footer{
	background-color: rgb(102, 102, 102);
	color: #000000;
}

body{
	margin: 0px;
}
main{
	margin: 20px;
}

h1{
	text-align: center;
}
h2{
	text-align: center;
}
h3{
	text-align: center;
}
h4{
	text-align: center;
}
h5{
	text-align: center;
}
h6{
	text-align: center;
}

.theme{
	position: relative;
	bottom: 140px;
	left: 50px;
}

.text{
	border-style: solid;
	border-radius: 20px;
	max-width: 50%;
	padding: 20px;
	margin: auto;
	margin-bottom: 20px;
	color: #000000;
	background-color: rgb(192, 192, 192);
}

.devcom{
	border-style: solid;
	border-radius: 20px;
	max-width: 50%;
	padding: 20px;
	margin: auto;
	margin-bottom: 20px;
	color: #000000;
	background-color: rgb(255, 255, 255);
}

.list{
	margin: auto;
	max-width: 50%;
}

 /* Style the button that is used to open and close the collapsible content */
.collapsible {
  background-color: #eee;
  color: #444;
  cursor: pointer;
  padding: 18px;
  width: 100%;
  border: none;
  text-align: left;
  outline: none;
  font-size: 15px;
}

/* Add a background color to the button if it is clicked on (add the .active class with JS), and when you move the mouse over it (hover) */
.active, .collapsible:hover {
  background-color: #ccc;
}

/* Style the collapsible content. Note: hidden by default */
.content {
  padding: 0 18px;
  background-color: white;
  max-height: 0;
  overflow: hidden;
  transition: max-height 0.2s ease-out;
}
""";
	}
}
