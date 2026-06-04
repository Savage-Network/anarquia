package com.minecraft.core.account.context.objects.joinmessage;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.joinmessage.JoinMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JoinMessageMetadata {

    private String selectedMessage = null;
    private long updatedAt = System.currentTimeMillis();

    public JoinMessageMetadata() {
    }

    public void setSelectedMessage(JoinMessage message) {
        if (message == null) {
            this.selectedMessage = null;
        } else {
            this.selectedMessage = message.getId();
        }
        updatedAt = System.currentTimeMillis();
    }

    public JoinMessage getSelectedJoinMessage() {
        if (selectedMessage == null) return null;
        return Core.getJoinMessageController().of(selectedMessage);
    }

    public String getMessage(RankType rank) {
        JoinMessage selected = getSelectedJoinMessage();
        
        if (selected != null && selected.hasAccess(rank)) {
            return selected.getMessage();
        }
        
        List<JoinMessage> available = getAvailableMessages(rank);
        if (available.isEmpty()) {
            JoinMessage defaultMsg = Core.getJoinMessageController().of("default");
            return defaultMsg != null ? defaultMsg.getMessage() : "entrou no lobby!";
        }
        
        return available.get((int) (Math.random() * available.size())).getMessage();
    }

    public List<JoinMessage> getAvailableMessages(RankType rank) {
        return Core.getJoinMessageController().list(rank);
    }
}
