package com.jimi.uw_server.service;

import java.io.File;
import java.util.List;

import com.jfinal.aop.Enhancer;
import com.jfinal.kit.PathKit;
import com.jimi.uw_server.constant.FAQSQL;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.Faq;
import com.jimi.uw_server.service.base.SelectService;

public class FAQService {

	public static FAQService me = new FAQService();
	
	public static SelectService selectService = Enhancer.enhance(SelectService.class);
	
	public String uploadPic(File file, String fileName) {
		
		String name = getPicName(fileName);
		String path = getFilePath("image", "");
		File dir = new File(path);
		
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File desFile = new File( path + name);
		if (desFile.exists()) {
			desFile.delete();
		}
		file.renameTo(desFile);
		System.out.println(desFile.getAbsolutePath());
		return "/image/" + name;
	}
	
	
	public void uploadFAQ(String title, String content, String resultHtml) {
		Faq faq = Faq.dao.findFirst(FAQSQL.GET_FAQ_BY_PROBLEM_NAME, title);
		if (faq != null) {
			throw new OperationException("添加失败，已存在相同的标题！");
		}
		faq = new Faq();
		faq.setProblemName(title);
		faq.setContent(content);
		faq.setResultHtml(resultHtml);
		faq.save();
	}
	
	
	public void updateFAQ(Integer id, String title, String content, String resultHtml) {
		Faq faq = Faq.dao.findById(id);
		if (faq == null) {
			throw new OperationException("更新失败，该条目不存在！");
		}
		faq.setProblemName(title);
		faq.setContent(content);
		faq.setResultHtml(resultHtml);
		faq.update();
	}
	
	
	public List<Faq> selectFAQ(String keyword) {
		List<Faq> faqs = null;
		if (keyword == null || keyword.trim().equals("")) {
			faqs = Faq.dao.find(FAQSQL.GET_ALL_FAQ);
		}else {
			faqs = Faq.dao.find(FAQSQL.GET_FAQ_BY_KEYWORD, "%"+keyword+"%", "%"+keyword+"%");
		}
		return faqs;
	}
	
	
	public void deleteFAQ(Integer id) {
		
		Faq faq = Faq.dao.findById(id);
		if (faq == null) {
			throw new OperationException("删除失败，该条目不存在！");
		}
		faq.delete();
	}
	
	
	private String getPicName(String string) {
		long time = System.currentTimeMillis();
		String endSuffix = string.substring(string.lastIndexOf("."), string.length());
		String name = String.valueOf(time) + endSuffix;
		return name;
	}
	
	
	/**
	 * 拼接文件路径（web根目录/basePath/params1/params2...）
	 * @param basePath
	 * @param params
	 * @return
	 */
	public String getFilePath(String basePath, String... params) {
		StringBuffer filePath = new StringBuffer();
		filePath.append(PathKit.getWebRootPath());
		filePath.append(File.separator);
		filePath.append(basePath);
		if (params != null && params.length > 0 && !params[0].equals("")) {
			for (String param : params) {
				filePath.append(File.separator);
				filePath.append(param);
			}
		}
		
		filePath.append(File.separator);
		return filePath.toString();

	}
}