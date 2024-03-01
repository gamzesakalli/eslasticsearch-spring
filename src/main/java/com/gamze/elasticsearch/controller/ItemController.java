package com.gamze.elasticsearch.controller;

import com.gamze.elasticsearch.dto.SearchRequestDto;
import com.gamze.elasticsearch.model.Item;
import com.gamze.elasticsearch.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    @PostMapping
    public Item createIndex(@RequestBody Item item){
        return itemService.createIndex(item);
    }
    //Add items from Json
    @PostMapping("/init-index")
    public void addItemsFromJson(){
         itemService.addItemsFromJson();
    }
    //Get All data from index
    @GetMapping("/getAllDataFromIndex/{indexName}")
    public List<Item> getAllDataFromIndex(@PathVariable String indexName){
        return itemService.getAllDataFromIndex(indexName);
    }
    //Search items by field and value
    @GetMapping("/search")
    public List<Item> searchItemsByFieldAndValue(@RequestBody SearchRequestDto dto){
        return itemService.searchItemsFieldAndValue(dto);
    }
    //Search items by name and brand with query
    @GetMapping("/search/{name}/{brand}")
    public List<Item> searchItemsByNameAndBrandWithQuery(@PathVariable String name,
                                                         @PathVariable String brand){
        return itemService.searchItemsByNameAndBrandWithQuery(name,brand);
    }
    //boolQuery
    @GetMapping("/boolQuery")
    public List<Item> boolQuery(@RequestBody SearchRequestDto searchRequestDto) {
        return itemService.boolQueryFieldAndValue(searchRequestDto);
    }

    @GetMapping("/autoSuggest/{name}")
    public Set<String> autoSuggestItemsByName(@PathVariable String name) {
        return itemService.findSuggestedItemNames(name);
    }

    @GetMapping("/suggestionsQuery/{name}")
    public List<String> autoSuggestItemsByNameWithQuery(@PathVariable String name) {
        return itemService.autoSuggestItemsByNameWithQuery(name);
    }

}
