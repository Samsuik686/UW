package com.jimi.uw_server.controller;

import java.util.List;

import com.jfinal.core.Controller;
import com.jfinal.upload.UploadFile;
import com.jimi.uw_server.exception.ParameterException;
import com.jimi.uw_server.model.Faq;
import com.jimi.uw_server.service.FAQService;
import com.jimi.uw_server.util.ResultUtil;


public class FAQController extends Controller {

	FAQService faqService = FAQService.me;


	public void uploadPic(UploadFile file) {
		file = getFile();
		if (file == null) {
			throw new ParameterException("参数不能为空！");
		}
		String fileName = file.getFileName();
		if (!(fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".jpeg"))) {
			throw new ParameterException("请上传正确的图片格式，仅支持JPG，PNG， JPEG！");
		}
		String result = faqService.uploadPic(file.getFile(), file.getFileName());
		renderJson(ResultUtil.succeed(result));
	}


	public void uploadFAQ(String title, String content, String resultHtml) {
		if (title == null || content == null) {
			throw new ParameterException("参数不能为空！");
		}
		faqService.uploadFAQ(title, content, resultHtml);
		renderJson(ResultUtil.succeed());

	}


	public void updateFAQ(Integer id, String title, String content, String resultHtml) {
		if (id == null || title == null || content == null) {
			throw new ParameterException("参数不能为空！");
		}
		faqService.updateFAQ(id, title, content, resultHtml);
		renderJson(ResultUtil.succeed());
	}


	public void selectFAQ(String keyword) {
		List<Faq> result = faqService.selectFAQ(keyword);
		renderJson(ResultUtil.succeed(result));
	}


	public void deleteFAQ(Integer id) {
		if (id == null) {
			throw new ParameterException("参数不能为空！");
		}
		faqService.deleteFAQ(id);
		renderJson(ResultUtil.succeed());
	}
}
