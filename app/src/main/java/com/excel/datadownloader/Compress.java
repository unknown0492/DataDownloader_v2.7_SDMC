package com.excel.datadownloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import android.util.Log;

public class Compress { 
	  private static final int BUFFER = 2048; 
	 
	  private String[] _files; 
	  private String _zipFile; 
	 
	  public Compress(String[] files, String zipFile) { 
	    _files = files; 
	    _zipFile = zipFile; 
	  } 
	  
	  static public void addFileToZip(String path, String srcFile, ZipOutputStream zip) throws Exception {
		    File folder = new File( srcFile );
		    if (folder.isDirectory()) {
		        addFolderToZip(path, srcFile, zip);
		    } else {
		        byte[] buf = new byte[1024];
		        int len;
		        FileInputStream in = new FileInputStream(srcFile);
		        zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
		        while ((len = in.read(buf)) > 0) {
		            zip.write(buf, 0, len);
		        }
		    }
		}
	  
	  static public void addFolderToZip(String path, String srcFolder,
		        ZipOutputStream zip) throws Exception {
		    File folder = new File(srcFolder);
		    for (String fileName : folder.list()) {
		        if (path.equals("")) {
		            addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
		        } else {
		            addFileToZip(path + "/" + folder.getName(), srcFolder + "/"
		                + fileName, zip);
		        }
		    }
		}
	  
	  static public void zipFolder(String srcFolder, String destZipFile)
		        throws Exception {
		    ZipOutputStream zip = null;
		    FileOutputStream fileWriter = null;
		    fileWriter = new FileOutputStream(destZipFile);
		    zip = new ZipOutputStream(fileWriter);
		    addFolderToZip("", srcFolder, zip);
		    zip.flush();
		    zip.close();
		}
	 
	  public void zip() { 
	    try  { 
	      BufferedInputStream origin = null; 
	      FileOutputStream dest = new FileOutputStream(_zipFile); 
	 
	      ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest)); 
	 
	      byte data[] = new byte[BUFFER]; 
	 
	      for(int i=0; i < _files.length; i++) { 
	        Log.v("Compress", "Adding: " + _files[i]); 
	        FileInputStream fi = new FileInputStream(_files[i]); 
	        origin = new BufferedInputStream(fi, BUFFER); 
	        ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1)); 
	        out.putNextEntry(entry); 
	        int count; 
	        while ((count = origin.read(data, 0, BUFFER)) != -1) { 
	          out.write(data, 0, count); 
	        } 
	        origin.close(); 
	      } 
	 
	      out.close(); 
	    } catch(Exception e) { 
	      e.printStackTrace(); 
	    } 
	 
	  } 

	public static void unZipIt( String zipFile, String outputFolder ){
		  
		     byte[] buffer = new byte[1024];
		 
		     try{
		 
		    	//create output directory is not exists
		    	File folder = new File( outputFolder );
		    	if(!folder.exists()){
		    		folder.mkdir();
		    	}
		 
		    	//get the zip file content
		    	ZipInputStream zis = 
		    		new ZipInputStream(new FileInputStream(zipFile));
		    	//get the zipped file list entry
		    	ZipEntry ze = zis.getNextEntry();
		 
		    	while(ze!=null){
		 
		    	   String fileName = ze.getName();
		           File newFile = new File(outputFolder + File.separator + fileName);
		 
		           System.out.println("file unzip : "+ newFile.getAbsoluteFile());
		 
		            //create all non exists folders
		            //else you will hit FileNotFoundException for compressed folder
		            new File(newFile.getParent()).mkdirs();
		            //new File(newFile.getParent()).;
		 
		            FileOutputStream fos = new FileOutputStream(newFile);             
		 
		            int len;
		            while ((len = zis.read(buffer)) > 0) {
		       		fos.write(buffer, 0, len);
		            }
		 
		            fos.close();   
		            ze = zis.getNextEntry();
		    	}
		 
		        zis.closeEntry();
		    	zis.close();
		 
		    	System.out.println("Done");
		 
		    }catch(IOException ex){
		       ex.printStackTrace(); 
		    }
	  }    
	
} 
