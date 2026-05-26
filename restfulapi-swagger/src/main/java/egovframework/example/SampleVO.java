/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package egovframework.example;

import org.egovframe.rte.ptl.reactive.validation.EgovNullCheck;
import io.swagger.v3.oas.annotations.media.Schema; // Swagger 3 어노테이션 추가

/**
 * @Class Name : SampleVO.java
 * @Description : SampleVO Class
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

@Schema(description = "샘플 게시물 데이터 모델 (VO)")
public class SampleVO extends SampleDefaultVO {

	private static final long serialVersionUID = 1L;

	/** 아이디 */
	@Schema(description = "게시물 아이디", example = "SAMPLE-00001")
	private String id;

	/** 이름 */
	@EgovNullCheck(message="{confirm.required.name}")
	@Schema(description = "게시물 제목(이름)", example = "Swagger 연동 테스트", requiredMode = Schema.RequiredMode.REQUIRED)
	private String name;

	/** 내용 */
	@EgovNullCheck(message="{confirm.required.description}")
	@Schema(description = "게시물 내용", example = "이것은 테스트 내용입니다.", requiredMode = Schema.RequiredMode.REQUIRED)
	private String description;

	/** 사용여부 */
	@Schema(description = "사용 여부 (Y/N)", example = "Y", defaultValue = "Y")
	private String useYn;

	/** 등록자 */
	@EgovNullCheck(message="{confirm.required.user}")
	@Schema(description = "등록자 아이디", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
	private String regUser;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUseYn() {
		return useYn;
	}

	public void setUseYn(String useYn) {
		this.useYn = useYn;
	}

	public String getRegUser() {
		return regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

}