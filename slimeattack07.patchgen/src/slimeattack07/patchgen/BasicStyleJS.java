package slimeattack07.patchgen;

public class BasicStyleJS {

	public static String getScript() {
		return """
var coll = document.getElementsByClassName("collapsible");
var i;
var newheight = 0;

for (i = 0; i < coll.length; i++) {
  coll[i].addEventListener("click", collapsible);
}

function collapsible() {
    this.classList.toggle("active");
    var content = this.nextElementSibling;

    if(this.classList.contains("active"))
    	newheight += content.scrollHeight;
    else
    	newheight -= content.scrollHeight;

    if (content.style.maxHeight)
      content.style.maxHeight = null;
    else
    	fixHeight(content);

    fixParent(content);
}

function fixHeight(element){
//	console.log(element.scrollHeight);
//	console.log("Its " + newheight);
  	element.style.maxHeight = newheight + "px";
}

function fixParent(element){
	var parent = element.parentNode;

	if(parent == null || parent === undefined || parent.classList === undefined)
		return;
		
//	console.log("tagname " + element.tagName)
	
	if(parent.classList.contains("biggest"))
		fixHeight(parent);
	else
		fixParent(parent);
}
""";
	}
}
