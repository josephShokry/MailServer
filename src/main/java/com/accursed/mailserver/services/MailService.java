package com.accursed.mailserver.services;

import com.accursed.mailserver.builders.DraftBuilder;
import com.accursed.mailserver.builders.ImmutableMailBuilder;
import com.accursed.mailserver.builders.MailBuilder;
import com.accursed.mailserver.dtos.MailDTO;
import com.accursed.mailserver.dtos.MailMapper;
import com.accursed.mailserver.models.DraftMail;
import com.accursed.mailserver.models.Folder;
import com.accursed.mailserver.models.ImmutableMail;
import com.accursed.mailserver.models.Mail;
import com.accursed.mailserver.models.User;
import com.accursed.mailserver.repositories.MailRepository;
import com.accursed.mailserver.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Optional;

@Service
public class MailService {
    @Autowired
    private MailRepository mailRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private FolderService folderService;

    private MailMapper mailMapper = Mappers.getMapper(MailMapper.class);

    // TODO refactor into director class
    public ImmutableMail sendMail(MailDTO dto){
        ImmutableMailBuilder mailBuilder = ImmutableMailBuilder.getInstance();
        mailBuilder.reset();
        mailBuilder.setMailFrom(userRepo.findByEmail(dto.from).get(0));
        mailBuilder.setMailTo(userRepo.findByEmail(dto.to).get(0));
        mailBuilder.setDate();
        mailBuilder.setContent(dto.content);
        mailBuilder.setPriority(dto.priority);
        mailBuilder.setSubject(dto.subject);
        mailBuilder.setIsStarred(dto.isStarred);
        mailBuilder.setState(dto.state);
        ImmutableMail mail = mailBuilder.getResult();
        mailRepo.save(mail);
        return mail;
    }

    public DraftMail sendDraft(MailDTO dto){
        DraftBuilder draftBuilder = DraftBuilder.getInstance();
        draftBuilder.reset();
        draftBuilder.setDate();
        draftBuilder.setContent(dto.content);
        draftBuilder.setPriority(dto.priority);
        draftBuilder.setSubject(dto.subject);
        draftBuilder.setIsStarred(dto.isStarred);
        draftBuilder.setState(dto.state);
        DraftMail mail = draftBuilder.getResult();
        mailRepo.save(mail);
        return mail;
    }
    // TODO test this when you can find by id
    public void updateDraft(MailDTO dto) {
        //TODO it is better to use getMail method of mailService class
        Optional<Mail> m = mailRepo.findById(dto.mailId);
        if(m.isPresent()) {
            mailMapper.updateMailFromDto(dto, (DraftMail) m.get());
            mailRepo.save(m.get());
        }
    }
    public Mail getMail(String id) {
        return mailRepo.findById(id).get();
    }

    public void addToFolder(MailDTO mailDTO) {
        Mail mail = getMail(mailDTO.mailId);
        Folder folder = folderService.getById(mailDTO.folderId);
        folder.addMail(mail);
        folderService.update(folder);
    }
}
