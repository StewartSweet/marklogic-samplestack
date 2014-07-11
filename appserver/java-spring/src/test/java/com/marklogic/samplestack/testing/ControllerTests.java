package com.marklogic.samplestack.testing;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.samplestack.domain.QnADocument;
import com.marklogic.samplestack.exception.SamplestackIOException;
import com.marklogic.samplestack.service.QnAService;

public class ControllerTests {

	private Logger logger = LoggerFactory.getLogger(ControllerTests.class);

	@Autowired
	private WebApplicationContext wac;

	@Autowired
	private FilterChainProxy springSecurityFilter;

	@Autowired
	protected ObjectMapper mapper;
	
	protected MockMvc mockMvc;

	protected HttpSession session;
	
	protected QnADocument askedQuestion;
	
	@Autowired
	protected QnAService qnaService;
	
	@PostConstruct
	public void setup() {
		if (this.mockMvc == null) {
			this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
				.addFilter(this.springSecurityFilter, "/*").build();
		}
	}
	
	protected JsonNode getTestJson(String testPath) {
		ClassPathResource r = new ClassPathResource(testPath);
		try {
			return mapper.readValue(r.getInputStream(), JsonNode.class);
		} catch (IOException e) {
			throw new SamplestackIOException(e);
		}
	}

	protected void login(String username, String password) throws Exception {
		this.session = mockMvc
				.perform(
						post("/login").param("username", username).param(
								"password", password))
				.andExpect(status().is(HttpStatus.FOUND.value()))
				.andExpect(redirectedUrl("/")).andReturn().getRequest()
				.getSession();

	}

	protected void logout() throws Exception {
		this.session = this.mockMvc.perform(get("/logout")).andReturn().getRequest()
				.getSession();
	}

	

	protected void askQuestion() throws Exception {
		if (askedQuestion == null) {
			login("joeUser@marklogic.com", "joesPassword");
	
			QnADocument qnaDoc = new QnADocument(mapper,
					"Question from contributor", "I ask questions", "tag1",
					"tag2");
	
			String payload = mapper.writeValueAsString(qnaDoc.getJson());
	
			// send a contributor to the questions endpoint
			String askedQuestion = this.mockMvc
					.perform(
							post("/questions")
									.session((MockHttpSession) session)
									.contentType(MediaType.APPLICATION_JSON)
									.content(payload))
					.andExpect(status().isCreated()).andReturn().getResponse()
					.getContentAsString();
			logger.debug(askedQuestion);
	
			ObjectNode node = mapper.readValue(askedQuestion, ObjectNode.class);
			this.askedQuestion = new QnADocument(node);
		}
	}

}