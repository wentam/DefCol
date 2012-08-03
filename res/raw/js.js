var currentPalette = 0;
var palette_selected = 0;

var downOn = 0;

function update() {
    $.getJSON('?action=getPalettes',function(data) {
	var html = '';
	for (var i = 0; i < data.length; i++) {
	    html += '<div id="'+i+'" palette_selected=1;update();" class=\"palette_item\">'+(data[i]['name'])+'</div>';
	}

	$('#leftbar').html(html);

	if (palette_selected == 1) {
	    $.getJSON('?action=getPaletteColors&id='+currentPalette,function(data) {
		
		var html = '';

		for (var i = 0; i < data.length; i++) {
		    html += '<div class="color" style="background-color:#'+data[i]+'">#'+data[i]+'</div>';
		}

		$('#rightbar').html(html);


		// iterate over colors, make the text contrast as white or black
		$('#rightbar .color').each(function(i){
		    var color = $(this);
		    
		    var rgb = color.css('background-color');

		    rgb = rgb.replace("rgb(", "");
		    rgb = rgb.replace(")", "");

		    var rgb_arr = rgb.split(",");


		    if (rgb2hsv(parseInt(rgb_arr[0]), parseInt(rgb_arr[1]), parseInt(rgb_arr[2]))['v'] > 50)  {
			color.css('color','#000000');
		    } else {
			color.css('color','#ffffff');
		    }
		});


	    });	    
	}
    });
}


function init() {
    update();
    setInterval('update()', 500);

    $(document).on("mousedown",'.palette_item',function () {
	// currentPalette = $(this).attr('id');
	downOn =  $(this).attr('id');
	// palette_selected = 1;
	// update();
    });

    $(document).on("mouseup",'.palette_item',function () {
	if (downOn == $(this).attr('id')) {
	    currentPalette = $(this).attr('id');
	    palette_selected = 1;
	    update();
	}
    });
}


function rgb2hsv () {
    var rr, gg, bb,
    r = arguments[0] / 255,
    g = arguments[1] / 255,
    b = arguments[2] / 255,
    h, s,
    v = Math.max(r, g, b),
    diff = v - Math.min(r, g, b),
    diffc = function(c){
	return (v - c) / 6 / diff + 1 / 2;
    };

    if (diff == 0) {
	h = s = 0;
    } else {
	s = diff / v;
        rr = diffc(r);
        gg = diffc(g);
        bb = diffc(b);

        if (r === v) {
            h = bb - gg;
        }else if (g === v) {
            h = (1 / 3) + rr - bb;
        }else if (b === v) {
            h = (2 / 3) + gg - rr;
        }
        if (h < 0) {
            h += 1;
        }else if (h > 1) {
            h -= 1;
        }
    }
    return {
        h: Math.round(h * 360),
        s: Math.round(s * 100),
        v: Math.round(v * 100)
    };
}