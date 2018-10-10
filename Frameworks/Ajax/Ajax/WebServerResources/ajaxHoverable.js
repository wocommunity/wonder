// Inspired by Michael Leigeber in a tutorial post here: 
// http://sixrevisions.com/tutorials/javascript_tutorial/create_lightweight_javascript_tooltip/
var erxToolTip=function(){
	var id = 'erxToolTip';
	var top = 3;
	var left = 3;
	var maxw = 300;
	var speed = 10;
	var timer = 20;
	var endalpha = 95;
	var alpha = 0;
	var tt,t,c,b,h;
	var ie = document.all ? true : false;
	var hoverActionElem = null;
	var offsetX = 0;
	var offsetY = 0;
	return{
	show:function(hoverActionElement, v, w, offsetXValue, offsetYValue){
		if (offsetXValue) {
			offsetX = offsetXValue;
		}
		
		if (offsetYValue) {
			offsetY = offsetYValue;
		}
		
		if(tt == null){
			tt = document.createElement('div');
			tt.setAttribute('id',id);
			t = document.createElement('div');
			t.setAttribute('id',id + 'Top');
			c = document.createElement('div');
			c.setAttribute('id',id + 'Contents');
			c.setAttribute('class', 'erxDefaultFont');
			b = document.createElement('div');
			b.setAttribute('id',id + 'Bottom');
			tt.appendChild(t);
			tt.appendChild(c);
			tt.appendChild(b);
			document.body.appendChild(tt);
			tt.style.opacity = 0;
			tt.style.filter = 'alpha(opacity=0)';		
			tt.style.position = 'absolute';
			tt.style.zIndex = 2000000;
		}
		tt.style.display = 'block';
		c.innerHTML = v;
		tt.style.width = w ? w : 'auto';
		if(!w && ie){
			t.style.display = 'none';
			b.style.display = 'none';
			tt.style.width = tt.offsetWidth;
			t.style.display = 'block';
			b.style.display = 'block';
		}
		
		// (Aaron) Use Prototype and the erxHoverArea action element
		hoverActionElem = hoverActionElement;
		if ( ! hoverActionElem.erxHoverAreaMouseMoveRegistered) {
			hoverActionElem.erxHoverAreaMouseMoveRegistered = true;
			Element.observe(hoverActionElem, 'mousemove', erxToolTip.pos);					
		}
		
		h = parseInt(tt.offsetHeight) + top;
		clearInterval(tt.timer);
		tt.timer = setInterval(function(){erxToolTip.fade(1)},timer);
	},
	pos:function(e){
		var u = ie ? event.clientY + document.documentElement.scrollTop : e.pageY;
		var l = ie ? event.clientX + document.documentElement.scrollLeft : e.pageX;
		
		// this raises the hoverable to the top
		//tt.style.top = (u - h) + 'px';
		
		// this lowers the hoverable to the bottom
		tt.style.top = (u + offsetY) + 'px';
		
		// this places the hoverable on the right
		tt.style.left = (l + left + offsetX) + 'px';		
	},
	fade:function(d){
		var a = alpha;
		if((a != endalpha && d == 1) || (a != 0 && d == -1)){
			var i = speed;
			if(endalpha - a < speed && d == 1){
				i = endalpha - a;
			}else if(alpha < speed && d == -1){
				i = a;
			}
			alpha = a + (i * d);
			tt.style.opacity = alpha * .01;
			tt.style.filter = 'alpha(opacity=' + alpha + ')';
		}else{
			clearInterval(tt.timer);
			if(d == -1){tt.style.display = 'none'}
		}
	},
	hide:function(){
		if(tt != null){
			clearInterval(tt.timer);
			tt.timer = setInterval(function(){erxToolTip.fade(-1)},timer);
		}
	}
	};
}();