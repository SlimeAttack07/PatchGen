package slimeattack07.patchgen;

public class BasicStyle {
	
	public static String getStyle() {
		return """
#body{
	background-color: #3986A4;
}
#header{
	background-color: #B77F4A;
	color: #000000;
}
#footer{
	background-color: #B77F4A;
	color: #000000;
}
main div{
	color: #000000;
	background-color: #696E78;
}

body{
	margin: 0px;
}
main{
	margin: 20px;
}

h2{
	text-align: center;
}
.theme{
	position: relative;
	bottom: 140px;
	left: 50px;
}
""";
	}
}
