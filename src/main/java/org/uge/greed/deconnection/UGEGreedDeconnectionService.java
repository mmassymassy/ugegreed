package org.uge.greed.deconnection;

import org.uge.greed.greed.UGEGreed;
import org.uge.greed.messaging.messages.UGEGreedPleaseReconnectMessage;

import java.util.ArrayList;

public class UGEGreedDeconnectionService {
    protected UGEGreed mainService;
    protected DeconnectionState state = DeconnectionState.IDLE;
    protected boolean isChildDisconecting = false ;
    protected int numberOfAwaitedReconnectedFromChildren = 0;

    public enum DeconnectionState {
        IDLE,
        REQUESTED_FROM_PARENT,
        ACCEPTED_WAITING_FOR_CHILDREN,
        DISCONNECTED
    }

    public UGEGreedDeconnectionService(UGEGreed mainService) {
        this.mainService = mainService;
    }

    public void setState(DeconnectionState state) {
        this.state = state;
    }

    public void setIsChildDisconecting(boolean isChildDisconecting) {
        this.isChildDisconecting = isChildDisconecting;
    }
    public void setNumberOfAwaitedReconnectedFromChildren(int n){
        this.numberOfAwaitedReconnectedFromChildren = n;
    }
    public int getNumberOfAwaitedReconnectedFromChildren(){
        return this.numberOfAwaitedReconnectedFromChildren;
    }

    public boolean descreaseNumberOfAwaitedReconnectedFromChildren(){
        this.numberOfAwaitedReconnectedFromChildren--;
        return this.numberOfAwaitedReconnectedFromChildren == 0;
    }

    public boolean getIsDisconnectionPossible(){
        return (this.state == DeconnectionState.IDLE && !this.isChildDisconecting);
    }

    public boolean isChildDisconecting() {
        return this.isChildDisconecting;
    }

    public DeconnectionState getState() {
        return this.state;
    }

    public void requestDeconnection() {
        if(this.mainService.getState() != UGEGreed.UGEGReedStat.JOINED){
            System.out.println("On Peut pas se deconnecter si l'etat est " + this.mainService.getState() );
            return;
        }

        if(this.mainService.getExcutionService().isBusy()){
            System.out.println("On ne peut pas se deconnecter si on est en train de traiter une requete");
            return;
        }

        this.state = DeconnectionState.REQUESTED_FROM_PARENT;
        this.mainService.setState(UGEGreed.UGEGReedStat.DISCONNECTING);
        this.mainService.getMessagingService().sendLeaveRequestToParent();
    }

}

