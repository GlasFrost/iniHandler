/*
 * Copyright (c) 2014 Luis Hartmann
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package inihandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * handles ini files
 * @author hartmann_luis
 */
public class IniHandler {
    
    /* Map of content of ini file */
    public Map<String, Map<String, String>> cfg = new HashMap();
    
    /* filename of ini file */
    private String filename = "";

    ////////// PUBLIC FUNCTIONS //////////
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("IniHandler is not supposed to be started as a stand-alone application.");
    }
    
    /**
     * constructor
     * @param newfilename 
     */
    public void IniHandler(String newfilename) {
        setFilename(newfilename);
        reload();
    }
    
    /**
     * set / change filename
     * @param newfilename
     */
    public void setFilename(String newfilename) {
        filename = newfilename;
    }
    
    /**
     * reload the file
     * @return (boolean) success
     */
    public boolean reload() {
        return loadFile();
    }
    
    /**
     * save 
     * @return 
     */
    public boolean save() {
        boolean success = true;
        BufferedWriter writer = null;
        try {
            File iniFile = new File(filename);

            // This will output the full path where the file will be written to...
            Logger.getLogger(IniHandler.class.getName()).log(Level.INFO, null, iniFile.getCanonicalPath());

            writer = new BufferedWriter(new FileWriter(iniFile));
            
            // loop through cfg
            for (Entry<String, Map<String, String>> entry : cfg.entrySet()) {
                String header = entry.getKey();
                Map<String, String> values = entry.getValue();
                
                //write section
                writer.write("["+header+"]");
                writer.newLine();
                for(Entry<String, String> keyValuePair : values.entrySet()) {
                    writer.write(keyValuePair.getKey()+"="+keyValuePair.getValue());
                    writer.newLine();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(IniHandler.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception ex) {
                Logger.getLogger(IniHandler.class.getName()).log(Level.INFO, null, ex);
            }
        }
        return success;
    }
    
    
    ////////// PRIVATE FUNCTIONS //////////
    
    /**
     * read file and call parser
     * @param filename
     * @return (boolean) success
     */
    private boolean loadFile() {
        Path path = Paths.get(filename);
        try {
            List<String> lines = Files.readAllLines(path, Charset.forName("UTF-8"));
            parseLines(lines);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(IniHandler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    /**
     * parser
     * @param lines 
     */
    private void parseLines(List<String> lines) {
        cfg.clear();
        String header = "";
        
        int line_counter = 1;
        for(String line : lines) {
            //check if line is a comment
            if(line.startsWith("#") || line.startsWith(";")) {
                //line is a comment: ignore
                continue;
            }
            
            //check if line is header
            if(line.startsWith("[") && line.endsWith("]")) {
                //line is a header
                header = line.replace("[", "").replace("]", "");
                continue;
            }
            
            //check if line is a valid key/value pair
            if(line.contains("=")) {
                //line is a valid key/value pair
                String[] keyValuePair = line.split("=",2);
                HashMap<String, String> helper = new HashMap();
                if(cfg.containsKey(keyValuePair[0])) {
                    helper = (HashMap<String, String>) cfg.get(header);
                }
                helper.put(keyValuePair[0], keyValuePair[1]);
                cfg.put(header, helper);
            }
            else {
                //line is not a valid key/value pair: ignore the line
                RuntimeException ex = new RuntimeException("Ignoring invalid line in config file (Line "+line_counter+")");
                Logger.getLogger(IniHandler.class.getName()).log(Level.WARNING, null, ex);
            }
            line_counter++;
        }
    }
    
}
