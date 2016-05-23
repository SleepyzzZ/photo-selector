package com.sleepyzzz.server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Servlet implementation class DetectionServlet
 */
@WebServlet("/DetectionServlet")
public class DetectionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private final static long MAX_FILE_SIZE = 50 * 1024 * 1024;
	
	private final static String[] allowedExt = new String[]
			{"jpg", "JPG", "JPEG", "jpeg"};
	
	private String result;
	
	private DiskFileItemFactory factory;
	
	private ServletFileUpload upload;
	
	private PrintWriter writer;
	
	private ArrayList<FileItem> list;
	
	private String clientIP;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DetectionServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
response.setContentType("text/html;charset=utf-8");
		
		this.clientIP = request.getRemoteAddr();
		
		factory = new DiskFileItemFactory();
		factory.setSizeThreshold(2*1024*1024);		//缓冲区
		factory.setRepository(new File(request.getSession()		//临时存放文件目录
				.getServletContext()
				.getRealPath("/")
				+ "UploadTemp"));
		
		upload = new ServletFileUpload(factory);		//用硬盘文件工厂实例化上传组件
		upload.setFileSizeMax(MAX_FILE_SIZE);		//最大上传尺寸
		
		writer = response.getWriter();
		result = "Upload sucess";
		
		try {
			
			list = (ArrayList<FileItem>) upload.parseRequest(request);
		} catch (FileUploadException e) {
			// TODO Auto-generated catch block
			if(e instanceof FileSizeLimitExceededException) {
				
				result = "File size exceeds a predetermined size: " + MAX_FILE_SIZE + "bytes";
				return;
			}
			e.printStackTrace();
		}
		
		if(list == null || list.size() == 0) {
			
			result = "Please select images to upload";
			return;
		}
		
		String fileName;
		Date now = new Date();
		DateFormat dateFormat = DateFormat.getDateInstance();
		String date = dateFormat.format(now);
		String destDirName = request.getSession()
									.getServletContext()
									.getRealPath("/")
									+ "RawImages\\" + date;
		File dir = new File(destDirName);
		if(!dir.exists() && !dir.isDirectory()) {
			
			dir.mkdirs();
		}
		/*if(!dir.mkdirs()) {
			
			result = "The server creates image folder failed";
			return;
		}*/
		
		for(FileItem item : list) {
			
			fileName = clientIP + "-" + item.getName();
			try {
				
				File file = new File(destDirName, fileName);
				if(file.exists()) {
					continue;
				}
					
				item.write(new File(destDirName, fileName));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				result = e.getMessage();
				e.printStackTrace();
			}
		}
	
		writer.print(result);
	}

}
