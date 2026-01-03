package ru.neustupov.advpost.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.neustupov.advpost.model.MessageResponse;
import ru.neustupov.advpost.service.postgres.MessageResponseService;

import java.util.Optional;

@Controller
@RequestMapping("/messageresponses")
public class MessageResponseController {

    private final MessageResponseService service;

    public MessageResponseController(MessageResponseService service) {
        this.service = service;
    }

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort,
            Model model) {

        String[] sortParams = sort.split(",");
        Sort.Direction direction = Sort.Direction.fromString(sortParams[1]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        Page<MessageResponse> pageResult = service.findAll(pageable);

        model.addAttribute("responses", pageResult.getContent());
        model.addAttribute("currentPage", pageResult.getNumber());
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalItems", pageResult.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);

        return "messageresponses/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("response", new MessageResponse());
        return "messageresponses/form";
    }

    @PostMapping
    public String create(@ModelAttribute MessageResponse response) {
        service.save(response);
        return "redirect:/messageresponses";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Optional<MessageResponse> opt = service.findById(id);
        if (opt.isPresent()) {
            model.addAttribute("response", opt.get());
            return "messageresponses/view";
        }
        return "redirect:/messageresponses";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Optional<MessageResponse> opt = service.findById(id);
        if (opt.isPresent()) {
            model.addAttribute("response", opt.get());
            return "messageresponses/form";
        }
        return "redirect:/messageresponses";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute MessageResponse response) {
        response.setId(id);
        service.save(response);
        return "redirect:/messageresponses";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        service.deleteById(id);
        return "redirect:/messageresponses";
    }
}
