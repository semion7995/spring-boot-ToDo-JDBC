package com.apress.todo;

import com.apress.todo.domain.ToDo;
import com.apress.todo.error.ToDoErrorHandler;
import com.apress.todo.repository.CommonRepository;
import com.apress.todo.repository.ToDoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ToDoRestClient {
    private RestTemplate restTemplate;
    private ToDoRestClientProperties properties;
    private CommonRepository<ToDo> repository;

    @Autowired
    public ToDoRestClient(CommonRepository<ToDo> repository) {
        this.repository = repository;
    }


    public ToDoRestClient(ToDoRestClientProperties properties) {
        this.restTemplate = new RestTemplate();

        this.restTemplate.setErrorHandler(new ToDoErrorHandler());

        this.properties = properties;
    }

    public Iterable<ToDo> findAll() throws URISyntaxException{
        RequestEntity<Iterable<ToDo>> requestEntity = new RequestEntity<Iterable<ToDo>>(HttpMethod.GET, new URI(properties.getUrl() + properties.getBasePath()));
        ResponseEntity<Iterable<ToDo>> response = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<Iterable<ToDo>>() {});
//
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }
        return repository.findAll();
    }
    public ToDo findById(String id){
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", id);
        ToDo toDo = restTemplate.getForObject(properties.getUrl() + properties.getBasePath() + "/{id}", ToDo.class, params);
        return repository.findById(id);
    }

    public ToDo upsert(ToDo toDo) throws URISyntaxException{
        RequestEntity<?> requestEntity = new RequestEntity<>(toDo, HttpMethod.POST, new URI(properties.getUrl() + properties.getBasePath()));
        ResponseEntity<?> response = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<ToDo>() {});

        if (response.getStatusCode() == HttpStatus.CREATED){
            return restTemplate.getForObject(response.getHeaders().getLocation(), ToDo.class);
        }

        return repository.save(toDo);
    }

    public ToDo setCompleted(String id) throws URISyntaxException{
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", id);
        restTemplate.postForObject(properties.getUrl() + properties.getBasePath() + "/{id}?_method=patch", null, ResponseEntity.class, params);
        ToDo toDo = findById(id);
        toDo.setCompleted(true);
        repository.save(toDo);
        return toDo;
    }

    public void delete(String id){
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", id);
        restTemplate.delete(properties.getUrl() + properties.getBasePath() + "/{id}", params);
        repository.delete(findById(id));
    }
}
