package egovframework.example;

import java.util.List;

import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import egovframework.example.sample.service.EgovSampleService;
import egovframework.example.sample.service.SampleVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

// Swagger 3 어노테이션 임포트
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @Class Name : EgovSampleController.java
 * @Description : EgovSample Controller Class
 * @Modification Information
 * @
 * @  수정일      수정자              수정내용
 * @ ---------   ---------   -------------------------------
 * @ 2009.03.16            최초생성
 *
 * @author 개발프레임웍크 실행환경 개발팀
 * @since 2009. 03.16
 * @version 1.0
 * @see
 *
 *  Copyright (C) by MOPAS All right reserved.
 */

@Tag(name = "EgovSample", description = "샘플 게시판 관리 API (MVC)")
@Controller
@Slf4j
public class EgovSampleController {

	/** EgovSampleService */
	@Resource(name = "sampleService")
	private EgovSampleService sampleService;

	/** EgovPropertyService */
	@Resource(name = "propertiesService")
	private EgovPropertyService propertiesService;

	@Operation(summary = "루트 페이지 이동", description = "인덱스 페이지 접속 시 글 목록 화면으로 이동한다.")
	@GetMapping("/")
	public String index(
			@ParameterObject @ModelAttribute("sampleVO") SampleVO sampleVO, 
			 ModelMap model) throws Exception {
		return this.selectSampleList(sampleVO, model);
	}

	/**
	 * 글 목록을 조회한다. (pageing)
	 * @param sampleVO - 조회할 정보가 담긴 SampleDefaultVO
	 * @param model
	 * @return "egovSampleList"
	 * @exception Exception
	 */
	@Operation(summary = "글 목록 조회", description = "글 목록을 페이징하여 조회한다.")
	@GetMapping("/egovSampleList.do")
	public String selectSampleList(
			@ParameterObject @ModelAttribute("sampleVO") SampleVO sampleVO, 
			 ModelMap model) throws Exception {

		/** EgovPropertyService.sample */
		sampleVO.setPageUnit(propertiesService.getInt("pageUnit"));
		sampleVO.setPageSize(propertiesService.getInt("pageSize"));

		/** pageing setting */
		PaginationInfo paginationInfo = new PaginationInfo();
		paginationInfo.setCurrentPageNo(sampleVO.getPageIndex());
		paginationInfo.setRecordCountPerPage(sampleVO.getPageUnit());
		paginationInfo.setPageSize(sampleVO.getPageSize());

		sampleVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
		sampleVO.setLastIndex(paginationInfo.getLastRecordIndex());
		sampleVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

		/** List */
		List<?> sampleList = sampleService.selectSampleList(sampleVO);
		model.addAttribute("resultList", sampleList);

		/** Count */
		int totCnt = sampleService.selectSampleListTotCnt(sampleVO);
		paginationInfo.setTotalRecordCount(totCnt);

		/** Pagination */
		model.addAttribute("paginationInfo", paginationInfo);

		return "sample/egovSampleList";
	}

	/**
	 * 글 등록 화면을 조회한다.
	 * @param sampleVO - 목록 조회조건 정보가 담긴 VO
	 * @param model
	 * @return "egovSampleRegister"
	 * @exception Exception
	 */
	@Operation(summary = "글 등록 화면 조회", description = "새로운 글을 작성할 수 있는 등록 화면을 조회한다.")
	@PostMapping("/addSampleView.do")
	public String addSampleView( 
			@ParameterObject @ModelAttribute("sampleVO") SampleVO sampleVO, 
			 Model model) throws Exception {

		model.addAttribute("sampleVO", sampleVO);

		return "sample/egovSampleRegister";
	}

	/**
	 * 글을 등록한다.
	 * @param sampleVO - 등록할 정보가 담긴 VO
	 * @param status
	 * @return "forward:/egovSampleList.do"
	 * @exception Exception
	 */
	@Operation(summary = "글 등록", description = "작성한 글 정보를 데이터베이스에 등록한다.")
	@PostMapping("/addSample.do")
	public String addSample(
			@ParameterObject @Valid @ModelAttribute("sampleVO") SampleVO sampleVO, 
			 BindingResult bindingResult, 
			 Model model, 
			 SessionStatus status) throws Exception {

		if (bindingResult.hasErrors()) {
			model.addAttribute("sampleVO", sampleVO);
			return "sample/egovSampleRegister";
		}

		sampleService.insertSample(sampleVO);
		status.setComplete();

		return "redirect:/egovSampleList.do";
	}

	/**
	 * 글 수정화면을 조회한다.
	 * @param id - 수정할 글 id
	 * @param model
	 * @return "egovSampleRegister"
	 * @exception Exception
	 */
	@Operation(summary = "글 수정 화면 조회", description = "특정 글의 상세 정보를 가져와 수정 화면을 조회한다.")
	@PostMapping("/updateSampleView.do")
	public String updateSampleView(
			@ParameterObject @ModelAttribute("sampleVO") SampleVO sampleVO, 
			 Model model) throws Exception {

		SampleVO detail = sampleService.selectSample(sampleVO);
		detail.setSearchCondition(sampleVO.getSearchCondition());
		detail.setSearchKeyword(sampleVO.getSearchKeyword());
		detail.setPageIndex(sampleVO.getPageIndex());

		model.addAttribute("sampleVO", detail);

		return "sample/egovSampleRegister";
	}

	/**
	 * 글을 수정한다.
	 * @param sampleVO - 수정할 정보가 담긴 VO
	 * @param status
	 * @return "forward:/egovSampleList.do"
	 * @exception Exception
	 */
	@Operation(summary = "글 수정", description = "기존 글의 정보를 수정하여 저장한다.")
	@PostMapping("/updateSample.do")
	public String updateSample(
			@ParameterObject @Valid @ModelAttribute("sampleVO") SampleVO sampleVO, 
			 BindingResult bindingResult,
			 Model model, 
			 RedirectAttributes redirectAttributes, 
			 SessionStatus status) throws Exception {

		if (bindingResult.hasErrors()) {
			model.addAttribute("sampleVO", sampleVO);
			return "sample/egovSampleRegister";
		}

		sampleService.updateSample(sampleVO);
		status.setComplete();

		redirectAttributes.addAttribute("searchCondition", sampleVO.getSearchCondition());
		redirectAttributes.addAttribute("searchKeyword", sampleVO.getSearchKeyword());
		redirectAttributes.addAttribute("pageIndex", sampleVO.getPageIndex());

		return "redirect:/egovSampleList.do";
	}

	/**
	 * 글을 삭제한다.
	 * @param sampleVO - 삭제할 정보가 담긴 VO
	 * @param status
	 * @return "forward:/egovSampleList.do"
	 * @exception Exception
	 */
	@Operation(summary = "글 삭제", description = "특정 글을 삭제 처리한다.")
	@PostMapping("/deleteSample.do")
	public String deleteSample(
			@ParameterObject @ModelAttribute("sampleVO") SampleVO sampleVO, 
			 RedirectAttributes redirectAttributes, 
			 SessionStatus status) throws Exception {

		sampleService.deleteSample(sampleVO);
		status.setComplete();

		redirectAttributes.addAttribute("searchCondition", sampleVO.getSearchCondition());
		redirectAttributes.addAttribute("searchKeyword", sampleVO.getSearchKeyword());
		redirectAttributes.addAttribute("pageIndex", sampleVO.getPageIndex());

		return "redirect:/egovSampleList.do";
	}

}