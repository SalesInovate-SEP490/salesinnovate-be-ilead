package fpt.capstone.SalesInnovate.iLead.service;

import fpt.capstone.proto.lead.GetLeadRequest;
import fpt.capstone.proto.lead.GetLeadResponse;
import fpt.capstone.proto.lead.LeadDtoProto;
import fpt.capstone.proto.lead.LeadServiceGrpc;
import fpt.capstone.proto.opportunity.*;
import fpt.capstone.proto.user.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeadsClientService {
    @GrpcClient("iOpportunity")
    OpportunityServiceGrpc.OpportunityServiceBlockingStub stubOpp ;

    @GrpcClient("iUser")
    UserServiceGrpc.UserServiceBlockingStub stubUser ;

    public boolean deleteRelationLeadCampaign (Long leadId){
        DeleteRelationLeadCampaignRequest request = DeleteRelationLeadCampaignRequest.newBuilder()
                .setLeadId(leadId)
                .build();
        DeleteRelationLeadCampaignResponse response = stubOpp.deleteRelationLeadCampaign(request);
        return response.getResponse();
    }

    public UserDtoProto getUser (String userId){
        GetUserRequest request = GetUserRequest.newBuilder()
                .setUserId(userId)
                .build();
        GetUserResponse response = stubUser.getUser(request);
        return response.getResponse();
    }

    public boolean createNotification (String userId, String content, Long linkedId,
                                       Long notificationType, List<String> listUser){
        CreateNotificationRequest.Builder requestBuilder = CreateNotificationRequest.newBuilder()
                .setUserId(userId)
                .setContent(content)
                .setLinkId(linkedId)
                .setNotificationType(notificationType);

        // Thêm từng phần tử từ listUser vào yêu cầu
        for (String user : listUser) {
            requestBuilder.addListUser(user);  // Sử dụng addListUser cho trường `repeated`
        }

        CreateNotificationResponse response = stubUser.createNotification(requestBuilder.build());
        return response.getResponse();
    }
}
