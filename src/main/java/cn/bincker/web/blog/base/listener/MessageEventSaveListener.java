package cn.bincker.web.blog.base.listener;

import cn.bincker.web.blog.base.entity.Message;
import cn.bincker.web.blog.base.event.MessageEvent;
import cn.bincker.web.blog.base.repository.IMessageRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class MessageEventSaveListener implements ApplicationListener<MessageEvent> {
    private final IMessageRepository messageRepository;

    public MessageEventSaveListener(IMessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public void onApplicationEvent(MessageEvent event) {
        var message = new Message();
        message.setContent(event.getContent());
        message.setType(event.getType());
        message.setFromUser(event.getFromUser());
        message.setToUser(event.getToUser());
        message.setRelatedTargetId(event.getRelatedTargetId());
        message.setOriginalTargetId(event.getOriginalTargetId());
        message.setTargetId(event.getTargetId());
        message.setIsRead(false);
        messageRepository.save(message);
    }
}
