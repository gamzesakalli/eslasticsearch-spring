package com.gamze.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.gamze.elasticsearch.dto.SearchRequestDto;
import com.gamze.elasticsearch.model.Item;
import com.gamze.elasticsearch.repository.ItemRepository;
import com.gamze.elasticsearch.util.ESUtil.ESUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final JsonDataService jsonDataService;
    private  final ElasticsearchClient elasticsearchClient;
    public Item createIndex(Item item) {
        return itemRepository.save(item);
    }

    public void addItemsFromJson() {
        log.info("Adding items from json");
        List<Item> itemList=jsonDataService.readItemsFromJson();
        itemRepository.saveAll(itemList);
    }

    public List<Item> getAllDataFromIndex(String indexName)  {
        var query= ESUtil.createMatchAllQuery();
        log.info("Elasticsearch query {}", query.toString());
        SearchResponse<Item> response=null;
        try{
           response =elasticsearchClient.search(
                    q -> q.index(indexName).query(query), Item.class);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
        log.info("Elasticsearch response {}", response);
        return extractItemsFromResponse(response);
        }
    public List<Item> extractItemsFromResponse(SearchResponse<Item> response){
        return response
                .hits()
                .hits()
                .stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    public List<Item> searchItemsFieldAndValue(SearchRequestDto dto) {
        Supplier<Query> query=ESUtil.buildQueryForFieldAndValue(dto.getFieldName().get(0),dto.getSearchValue().get(0));
        log.info("Elasticsearch query{} ",query.toString());
        SearchResponse<Item> response =null;
        try {
            response = elasticsearchClient.search(q -> q.index("items_index").query(query.get()), Item.class);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
        log.info("Elasticsearch response {}", response);
        return extractItemsFromResponse(response);
    }

    public List<Item> searchItemsByNameAndBrandWithQuery(String name, String brand) {
        return itemRepository.searchByNameAndBrand(name,brand);
    }

    public List<Item> boolQueryFieldAndValue(SearchRequestDto searchRequestDto) {
        try {
            var supplier = ESUtil.createBoolQuery(searchRequestDto);
            log.info("Elasticsearch query: " + supplier.get().toString());

            SearchResponse<Item> response = elasticsearchClient.search(q ->
                    q.index("items_index").query(supplier.get()), Item.class);
            log.info("Elasticsearch response: {}", response.toString());

            return extractItemsFromResponse(response);
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
    public Set<String> findSuggestedItemNames(String itemName) {
        Query autoSuggestQuery = ESUtil.buildAutoSuggestQuery(itemName);
        log.info("Elasticsearch query: {}", autoSuggestQuery.toString());

        try {
            return elasticsearchClient.search(q -> q.index("items_index").query(autoSuggestQuery), Item.class)
                    .hits()
                    .hits()
                    .stream()
                    .map(Hit::source)
                    .map(Item::getName)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> autoSuggestItemsByNameWithQuery(String name) {
        List<Item> items = itemRepository.customAutocompleteSearch(name);
        log.info("Elasticsearch response: {}", items.toString());
        return items
                .stream()
                .map(Item::getName)
                .collect(Collectors.toList());
    }
/*
    public List<Item> extractItemsFromResponse(SearchResponse<Item> response) {
        return response
                .hits()
                .hits()
                .stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

 */
}

