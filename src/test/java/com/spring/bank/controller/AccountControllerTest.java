package com.spring.bank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.bank.model.Account;
import com.spring.bank.model.AccountStatus;
import com.spring.bank.model.Currency;
import com.spring.bank.model.TimeFrame;
import com.spring.bank.service.AccountService;
import com.spring.bank.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AccountControllerTest {

    public static final String CONTENT_TYPE_JSON = "application/json";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @MockBean
    private TransactionService transactionService;

    @Value("${user.read.oauth.clientId}")
    private String userReadClientId;

    @Value("${user.read.oauth.clientSecret}")
    private String userReadClientSecret;

    @Value("${user.write.oauth.clientId}")
    private String userWriteClientId;

    @Value("${user.write.oauth.clientSecret}")
    private String userWriteClientSecret;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .addFilter(springSecurityFilterChain).build();
    }

    @Test
    public void when_no_token_then_unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                .content(objectMapper.writeValueAsBytes(createAccount()))
                .contentType(CONTENT_TYPE_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void when_token_but_not_correct_scope_then_forbidden() throws Exception {
        String accessToken = obtainAccessToken(userReadClientId, userReadClientSecret);
        mockMvc.perform(post("/api/v1/accounts")
                .header("Authorization", "Bearer " + accessToken)
                .content(objectMapper.writeValueAsBytes(createAccount()))
                .contentType(CONTENT_TYPE_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void when_post_to_create_valid_account_then_correct_response() throws Exception {
        String accessToken = obtainAccessToken(userWriteClientId, userWriteClientSecret);
        mockMvc.perform(post("/api/v1/accounts")
                .header("Authorization", "Bearer " + accessToken)
                .content(objectMapper.writeValueAsBytes(createAccount()))
                .contentType(CONTENT_TYPE_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void when_post_to_create_invalid_account_then_return_bad_request() throws Exception {
        String accessToken = obtainAccessToken(userWriteClientId, userWriteClientSecret);
        mockMvc.perform(post("/api/v1/accounts")
                .header("Authorization", "Bearer " + accessToken)
                .content(objectMapper.writeValueAsBytes(createAccountWithMissingFields()))
                .contentType(CONTENT_TYPE_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("[\"Currency is mandatory\"]"));
    }

    @Test
    public void when_post_to_create_valid_account_then_check_data_on_account_created() throws Exception {
        String accessToken = obtainAccessToken(userWriteClientId, userWriteClientSecret);
        mockMvc.perform(post("/api/v1/accounts")
                .header("Authorization", "Bearer " + accessToken)
                .content(objectMapper.writeValueAsBytes(createAccount()))
                .contentType(CONTENT_TYPE_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountService, times(1)).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getName()).isEqualTo("account name test");
        assertThat(accountCaptor.getValue().getCurrency()).isEqualTo(Currency.EUR);
        assertThat(accountCaptor.getValue().getBalance()).isEqualTo(new BigDecimal(9));
        assertThat(accountCaptor.getValue().getTransactions().size()).isEqualTo(2);
        assertThat(accountCaptor.getValue().getIban()).isNotEmpty();
        assertThat(accountCaptor.getValue().getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    public void when_put_to_cancel_account_then_correct_response() throws Exception {
        String accessToken = obtainAccessToken(userWriteClientId, userWriteClientSecret);
        when(accountService.findById(100L)).thenReturn(Optional.ofNullable(createAccount()));
        mockMvc.perform(put("/api/v1/accounts/{id}", 100)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(CONTENT_TYPE_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void when_put_to_cancel_account_then_expec_not_found() throws Exception {
        String accessToken = obtainAccessToken(userWriteClientId, userWriteClientSecret);
        mockMvc.perform(put("/api/v1/accounts/{id}", 100)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(CONTENT_TYPE_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Account with id 100 not found"));;
    }

    @Test
    public void when_put_to_cancel_account_then_check_data_on_account_updated() throws Exception {
        String accessToken = obtainAccessToken(userWriteClientId, userWriteClientSecret);
        when(accountService.findById(100L)).thenReturn(Optional.ofNullable(createAccount()));
        mockMvc.perform(put("/api/v1/accounts/{id}", 100)
                .header("Authorization", "Bearer " + accessToken)
                .content(objectMapper.writeValueAsBytes(createAccount()))
                .contentType(CONTENT_TYPE_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountService, times(1)).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getAccountStatus()).isEqualTo(AccountStatus.CLOSED);
    }

    @Test
    public void when_get_to_retrieve_transactions_then_correct_response() throws Exception {
        String accessToken = obtainAccessToken(userReadClientId, userReadClientSecret);
        mockMvc.perform(get("/api/v1/accounts/{id}/transactions", 100)
                .header("Authorization", "Bearer " + accessToken)
                .content(objectMapper.writeValueAsBytes(createAccount()))
                .param("timeFrame", TimeFrame.DAYS.name())
                .param("interval", String.valueOf(1))
                .contentType(CONTENT_TYPE_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void when_get_to_retrieve_transactions_with_missing_param_then_bad_request() throws Exception {
        String accessToken = obtainAccessToken(userReadClientId, userReadClientSecret);
        mockMvc.perform(get("/api/v1/accounts/{id}/transactions", 100)
                .header("Authorization", "Bearer " + accessToken)
                .content(objectMapper.writeValueAsBytes(createAccount()))
                .param("timeFrame", TimeFrame.DAYS.name())
                .contentType(CONTENT_TYPE_JSON))
                .andExpect(status().isBadRequest());
    }

    private String obtainAccessToken(String clientId, String clientSecret) throws Exception {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");

        ResultActions result
                = mockMvc.perform(post("/oauth/token")
                .params(params)
                .with(httpBasic(clientId, clientSecret))
                .accept(CONTENT_TYPE_JSON))
                .andExpect(status().isOk());

        String resultString = result.andReturn().getResponse().getContentAsString();

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        return jsonParser.parseMap(resultString).get("access_token").toString();
    }

    private Account createAccount() {
        Account account = new Account();
        account.setName("account name test");
        account.setCurrency(Currency.EUR);
        account.setCustomer("Cosmin");
        return account;
    }

    private Account createAccountWithMissingFields() {
        Account account = new Account();
        account.setName("account name test");
        account.setCustomer("Cosmin");
        return account;
    }

}
