package com.example.runnerz.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

// MockRestServiceServer fakes the external API, so this test never goes to the internet
@RestClientTest(UserRestClient.class)
class UserRestClientTest {

    @Autowired
    UserRestClient client;

    @Autowired
    MockRestServiceServer server;

    @Test
    void findByIdReadsTheUser() {
        server.expect(requestTo("https://jsonplaceholder.typicode.com/users/1"))
                .andRespond(withSuccess("""
                        {"id": 1, "name": "Test Runner", "username": "runner1",
                         "email": "runner@example.com", "address": {"city": "Anywhere"}}
                        """, MediaType.APPLICATION_JSON));

        User user = client.findById(1);

        assertThat(user.name()).isEqualTo("Test Runner");
        assertThat(user.email()).isEqualTo("runner@example.com");
    }

    @Test
    void findAllReadsTheList() {
        server.expect(requestTo("https://jsonplaceholder.typicode.com/users"))
                .andRespond(withSuccess("""
                        [{"id": 1, "name": "A"}, {"id": 2, "name": "B"}]
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.findAll()).extracting(User::name).containsExactly("A", "B");
    }
}
