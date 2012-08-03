//////////////////////////////////////////////////////////////////////////////
// Copyright 2012 Matthew Egeler
// 									       
// Licensed under the Apache License, Version 2.0 (the "License");	       
// you may not use this file except in compliance with the License.	       
// You may obtain a copy of the License at				      
// 									       
//     http://www.apache.org/licenses/LICENSE-2.0			       
// 									       
// Unless required by applicable law or agreed to in writing, software      
// distributed under the License is distributed on an "AS IS" BASIS,	       
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and      
// limitations under the License.					      
//////////////////////////////////////////////////////////////////////////////

package com.wentam.defcol;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import android.util.Log;

import android.content.Context;

import android.os.Environment;

// TODO implement cacheing in memory for obvious performance reasons
public class PaletteFile {
    private File file;
    private Context c;

    private String palettes_file;
    private int palette_count;

    public PaletteFile() {
	palette_count = getPaletteCount();
    }

    public int getPaletteCount() {
	final String[] palettes = readPalleteFile().split("\n");
	int count = 0;
	if (palettes[0] != "") {
	    for (String palette : palettes) {
		count += 1;
	    }
	}

	return count;
    }

    public ArrayList<String> getRows(){
	int[] tmp = {0,1,2};
    	return getRows(tmp);
    }

    public ArrayList<String> getRows(int[] cols) {
	final String[] palettes = readPalleteFile().split("\n");
	final ArrayList<String> palette_rows = new ArrayList<String>();

	if (palettes[0] != "") {
	    for (String palette : palettes) {
		String stringToAdd = "";
		String[] palette_row = palette.split("\\|");
		for (int col : cols) {
		    stringToAdd += palette_row[col]; 
		}
		palette_rows.add(stringToAdd);
	    }
	}

	return palette_rows;
    }

    public String getRow(int palette_id) {
	int[] tmp = {0,1,2};
	return getRow(palette_id,tmp);
    }

    public String getRow(int palette_id,int[] cols) {
	final String[] palettes = readPalleteFile().split("\n");

	if (palettes.length > palette_id) {
	    String finalString = "";

	    String[] palette_row = palettes[palette_id].split("\\|");
	    if (palette_row.length >= 2) {
		for (int col : cols) {
		    finalString += palette_row[col]; 
		}
	    }
	    
	    return finalString;
	} else {
	    return "";
	}
    }

    public void addNewPalette(String name, String defaultColor) {	
	getFile();

	FileWriter f = null;
	try {
	    f = new FileWriter(file, true);
	    f.append(name+"|"+defaultColor+"|\n");	 
	    f.close();
	} catch (IOException e) {
	    Log.w("DEFCOL", "Error writing " + file, e);
	}
    }

    public void addNewColor(int palette_id, String color) {
	// edit line
	String[] palettes = readPalleteFile().split("\n");
	
	String[] oldData = palettes[palette_id].split("\\|");

	if (oldData[1].substring(oldData[1].length()-1) != ".") {
	    oldData[1] += ".";
	}

	String newColors = oldData[1]+color;

	if (oldData.length >= 3) {
	    palettes[palette_id] = oldData[0]+"|"+newColors+"|"+oldData[2];
	} else {
	    palettes[palette_id] = oldData[0]+"|"+newColors+"|";
	}

						  
	FileWriter f = null;
	try {
	    f = new FileWriter(file);
	    for (String palette : palettes) {
		f.write(palette+"\n");							
	    }
	    f.close();
	} catch (IOException e) {
	    Log.w("DEFCOL", "Error writing " + file, e);
	}
    }

    public void changeColor(int palette_id, int color_id, String newColor) {
	String[] palettes = readPalleteFile().split("\n");
	
	String[] oldData = palettes[palette_id].split("\\|");
	String[] colors = oldData[1].split("\\.");
	
	ArrayList<String> tmp = new ArrayList<String>(Arrays.asList(colors));
	tmp.set(color_id,newColor);

	colors = tmp.toArray(new String[tmp.size()]);

	String newColors = "";
	
	int i = 0;
	for (String color : colors) {
	    if (i < colors.length-1) {
		newColors += color+".";
	    } else {
		newColors += color;
	    }
	    i++;
	}       	

	if (oldData.length >= 3) {
	    palettes[palette_id] = oldData[0]+"|"+newColors+"|"+oldData[2];
	} else {
	    palettes[palette_id] = oldData[0]+"|"+newColors+"|";
	}


	FileWriter f = null;
	try {
	    f = new FileWriter(file);
	    for (String palette : palettes) {
		f.write(palette+"\n");							
	    }
	    f.close();
	} catch (IOException e) {
	    Log.w("DEFCOL", "Error writing " + file, e);
	}
    }

    public void deleteColor(int palette_id, int color_id) {
	String[] palettes = readPalleteFile().split("\n");
	
	String[] oldData = palettes[palette_id].split("\\|");
	String[] colors = oldData[1].split("\\.");
	
	if (colors.length > 1) {
	
	    ArrayList<String> tmp = new ArrayList<String>(Arrays.asList(colors));
	    tmp.remove(color_id);
	    colors = tmp.toArray(new String[tmp.size()]);

	    String newColors = "";
	
	    int i = 0;
	    for (String color : colors) {
		if (i < colors.length-1) {
		    newColors += color+".";
		} else {
		    newColors += color;
		}
		i++;
	    }       	

	    if (oldData.length >= 3) {
		palettes[palette_id] = oldData[0]+"|"+newColors+"|"+oldData[2];
	    } else {
		palettes[palette_id] = oldData[0]+"|"+newColors+"|";
	    }


	    FileWriter f = null;
	    try {
		f = new FileWriter(file);
		for (String palette : palettes) {
		    f.write(palette+"\n");							
		}
		f.close();
	    } catch (IOException e) {
		Log.w("DEFCOL", "Error writing " + file, e);
	    }
	}
    }

    public void swapColors(int palette_id, int color_1, int color_2) {
	String[] palettes = readPalleteFile().split("\n");
	
	String[] oldData = palettes[palette_id].split("\\|");

	String[] oldColors = oldData[1].split("\\.");

	String color_1_val = new String(oldColors[color_1]);
	String color_2_val = new String(oldColors[color_2]);

	oldColors[color_1] = color_2_val;
	oldColors[color_2] = color_1_val;

	String newColors = "";
	for (String color : oldColors) {
	    newColors += color+".";
	}

	oldData[1] = newColors;

	if (oldData.length >= 3) {
	    palettes[palette_id] = oldData[0]+"|"+oldData[1]+"|"+oldData[2];
	} else {
	    palettes[palette_id] = oldData[0]+"|"+oldData[1]+"|";
	}
						  
	FileWriter f = null;
	try {
	    f = new FileWriter(file);
	    for (String palette : palettes) {
		f.write(palette+"\n");							
	    }
	    f.close();
	} catch (IOException e) {
	    Log.w("DEFCOL", "Error writing " + file, e);
	}
    }

    public void setColors(int palette_id, ArrayList<Integer> colors) {
	String[] mColors = new String[colors.size()];

	int i = 0;
	Iterator iterator = colors.iterator();
	while (iterator.hasNext()) {
	    mColors[i] = Integer.toHexString(((Integer)iterator.next()).intValue());
	    i++;
	}

	String newColors = "";
	
	i = 0;
	for (String color : mColors) {
	    color = color.substring(2);

	    if (i < colors.size()-1) {
		newColors += color+".";
	    } else {
		newColors += color;
	    }
	    i++;
	}

	String[] palettes = readPalleteFile().split("\n");		
	String[] oldData = palettes[palette_id].split("\\|");

	if (oldData.length >= 3) {
	    palettes[palette_id] = oldData[0]+"|"+newColors+"|"+oldData[2];
	} else {
	    palettes[palette_id] = oldData[0]+"|"+newColors+"|";
	}
						  
	FileWriter f = null;
	try {
	    f = new FileWriter(file);
	    for (String palette : palettes) {
		f.write(palette+"\n");							
	    }
	    f.close();
	} catch (IOException e) {
	    Log.w("DEFCOL", "Error writing " + file, e);
	}
    }

    public void deletePalette(int palette_id) {
	getFile();
	
	String[] palettes = readPalleteFile().split("\n");
						  
	ArrayList<String> palettes_list = new ArrayList<String>(Arrays.asList(palettes));
	palettes_list.remove(palette_id);

	palettes = palettes_list.toArray(new String[palettes_list.size()]);
						  
	FileWriter f = null;
	try {
	    f = new FileWriter(file);
	    for (String palette : palettes) {
		f.write(palette+"\n");							
	    }
	    f.close();
	} catch (IOException e) {
	    Log.w("DEFCOL", "Error writing " + file, e);
	}
    }

    public void renamePalette(int palette_id, String new_name) {
	// edit line
	String[] palettes = readPalleteFile().split("\n");
	
	String[] oldData = palettes[palette_id].split("\\|");

	if (oldData.length >= 3) {
	    palettes[palette_id] = new_name+"|"+oldData[1]+"|"+oldData[2];
	} else {
	    palettes[palette_id] = new_name+"|"+oldData[1]+"|";
	}
						  
	FileWriter f = null;
	try {
	    f = new FileWriter(file);
	    for (String palette : palettes) {
		f.write(palette+"\n");							
	    }
	    f.close();
	} catch (IOException e) {
	    Log.w("DEFCOL", "Error writing " + file, e);
	}
    }

    public File getFile() {
	if (file != null) {
	    return file;
	} else {
	    // get our path, creating it if it doesn't exist
	    File sdCard = Environment.getExternalStorageDirectory();
	    File dir = new File (sdCard.getAbsolutePath() + "/DefCol");
	    dir.mkdir();

	    // get file object
	    file = new File(dir, "palettes");

	    return file;
	}
    }

    private String readPalleteFile () {
	// check if we can read and write to the sd card
	String state = Environment.getExternalStorageState();
	if (Environment.MEDIA_MOUNTED.equals(state)) {
	    // we can read and write, do nothing
	} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	    // we can only read, give error
	} else {
	    // we can't read or write, give error
	}

	getFile();

	// read palettes file and store it in memory for parsing
	palettes_file = "";

	BufferedReader br = null;
	try {
	    br = new BufferedReader(new FileReader(file));
	    String tmp ;
	    while ((tmp = br.readLine()) != null) {
		palettes_file += tmp +"\n";
	    }
	    br.close();
	} catch (IOException e) {
	    Log.e("DEFCOL", "Error reading " + file, e);
	}

	return palettes_file;
    }
}