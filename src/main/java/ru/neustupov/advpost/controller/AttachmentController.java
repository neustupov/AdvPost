package ru.neustupov.advpost.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.neustupov.advpost.model.Attachment;
import ru.neustupov.advpost.service.postgres.AttachmentService;

import java.util.Optional;

@Controller
@RequestMapping("/attachments")
public class AttachmentController {

    private final AttachmentService service;

    public AttachmentController(AttachmentService service) {
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

        Page<Attachment> attachmentPage = service.findAll(pageable);

        model.addAttribute("attachments", attachmentPage.getContent());
        model.addAttribute("currentPage", attachmentPage.getNumber());
        model.addAttribute("totalPages", attachmentPage.getTotalPages());
        model.addAttribute("totalItems", attachmentPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);

        return "attachments/list";
    }

    @GetMapping("/{id}")
    public String read(@PathVariable Long id, Model model) {
        Optional<Attachment> attachment = service.findById(id);
        if (attachment.isPresent()) {
            model.addAttribute("attachment", attachment.get());
            return "attachments/view";
        }
        return "redirect:/attachments";
    }

    // Create: Form
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("attachment", new Attachment());
        return "attachments/form";
    }

    // Create: Save
    @PostMapping
    public String create(@ModelAttribute Attachment attachment) {
        service.save(attachment);
        return "redirect:/attachments";
    }

    // Update: Form
    @GetMapping("/{id}/edit")
    public String updateForm(@PathVariable Long id, Model model) {
        Optional<Attachment> attachment = service.findById(id);
        if (attachment.isPresent()) {
            model.addAttribute("attachment", attachment.get());
            return "attachments/form";
        }
        return "redirect:/attachments";
    }

    // Update: Save
    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Attachment attachment) {
        attachment.setId(id);
        service.save(attachment);
        return "redirect:/attachments";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        service.deleteById(id);
        return "redirect:/attachments";
    }
}
