package fpt.capstone.SalesInnovate.iLead.controller;

import fpt.capstone.SalesInnovate.iLead.dto.request.AddressInformationDTO;
import fpt.capstone.SalesInnovate.iLead.dto.request.LeadSalutionDTO;
import fpt.capstone.SalesInnovate.iLead.dto.response.LeadResponse;
import fpt.capstone.SalesInnovate.iLead.service.LeadsService;

import fpt.capstone.proto.lead.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

@GrpcService
public class LeadsGrpcController extends LeadServiceGrpc.LeadServiceImplBase {

    @Autowired
    LeadsService leadsService;

    @Override
    public void getLead (GetLeadRequest request, StreamObserver<GetLeadResponse> responseObserver){
        long leadId = request.getLeadId();
        LeadResponse leadResponse = leadsService.getLeadDetail(leadId);
        try {
            GetLeadResponse getLeadResponse ;

            if(leadResponse != null){
                LeadDtoProto proto = convertLeadResponseToProto(leadResponse);
                getLeadResponse = GetLeadResponse.newBuilder()
                        .setResponse(proto)
                        .build();
            }else{
                getLeadResponse = GetLeadResponse.getDefaultInstance();
            }
            responseObserver.onNext(getLeadResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.asRuntimeException());
        }
    }

    @Override
    public void getAddressInformation (GetAddressInformationRequest request,
                                       StreamObserver<GetAddressInformationResponse> responseObserver){
        long id = request.getAddressInformationId();
        AddressInformationDTO addressInformationDTO = leadsService.getAddressInformationById(id);
        try {
            GetAddressInformationResponse getAddressInformationResponse ;

            if(addressInformationDTO != null){
                AddressInformationDtoProto proto =  AddressInformationDtoProto.newBuilder()
                        .setAddressInformationId(addressInformationDTO.getAddressInformationId())
                        .setStreet(addressInformationDTO.getStreet())
                        .setCity(addressInformationDTO.getCity())
                        .setProvince(addressInformationDTO.getProvince())
                        .setPostalCode(addressInformationDTO.getPostalCode())
                        .setCountry(addressInformationDTO.getCountry())
                        .build();
                getAddressInformationResponse = GetAddressInformationResponse.newBuilder()
                        .setResponse(proto)
                        .build();
            }else{
                getAddressInformationResponse = GetAddressInformationResponse.getDefaultInstance();
            }
            responseObserver.onNext(getAddressInformationResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.asRuntimeException());
        }
    }

    @Override
    public void getSalution (GetSalutionRequest request,
                                       StreamObserver<GetSalutionResponse> responseObserver){
        long id = request.getLeadSalutionId();
        LeadSalutionDTO leadSalutionDTO = leadsService.getLeadSalutionById(id);
        try {
            GetSalutionResponse getSalutionResponse ;

            if(leadSalutionDTO != null){
                LeadSalutionDtoProto proto = LeadSalutionDtoProto.newBuilder()
                        .setLeadSalutionId(leadSalutionDTO.getLeadSalutionId())
                        .setLeadSalutionName(leadSalutionDTO.getLeadSalutionName())
                        .build();
                getSalutionResponse = GetSalutionResponse.newBuilder()
                        .setResponse(proto)
                        .build();
            }else{
                getSalutionResponse = GetSalutionResponse.getDefaultInstance();
            }
            responseObserver.onNext(getSalutionResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.asRuntimeException());
        }
    }

    private LeadDtoProto convertLeadResponseToProto (LeadResponse leadResponse){
        return  LeadDtoProto.newBuilder()
                .setLeadId(leadResponse.getLeadId())
                .setSource(leadResponse.getSource()==null?LeadSourceDtoProto.newBuilder()
                        .setLeadSourceId(0)
                        .build():LeadSourceDtoProto.newBuilder()
                        .setLeadSourceId(leadResponse.getSource().getLeadSourceId())
                        .build())
                .setIndustry(leadResponse.getIndustry()==null
                        ?IndustryDtoProto.newBuilder()
                        .setIndustryId(0)
                        .build()
                        :IndustryDtoProto.newBuilder()
                        .setIndustryId(leadResponse.getIndustry().getIndustryId())
                        .build())
                .setNoEmployee(leadResponse.getNoEmployee()==null?0:leadResponse.getNoEmployee())
                .setStatus(leadResponse.getStatus()==null
                        ?LeadStatusDtoProto.newBuilder()
                        .setLeadStatusId(0)
                        .build()
                        :LeadStatusDtoProto.newBuilder()
                        .setLeadStatusId(leadResponse.getStatus().getLeadStatusId())
                        .build())
                .setRating(leadResponse.getRating()==null
                        ?LeadRatingDtoProto.newBuilder()
                        .setLeadRatingId(0)
                        .build()
                        :LeadRatingDtoProto.newBuilder()
                        .setLeadRatingId(leadResponse.getRating().getLeadRatingId())
                        .build())
                .setAddressInfor(leadResponse.getAddressInfor()==null
                        ?AddressInformationDtoProto.newBuilder()
                        .setAddressInformationId(0)
                        .build()
                        :AddressInformationDtoProto.newBuilder()
                        .setAddressInformationId(leadResponse.getAddressInfor().getAddressInformationId())
                        .build())
                .setSalution(leadResponse.getSalution()==null
                        ?LeadSalutionDtoProto.newBuilder()
                        .setLeadSalutionId(0)
                        .build()
                        :LeadSalutionDtoProto.newBuilder()
                        .setLeadSalutionId(leadResponse.getSalution().getLeadSalutionId())
                        .build())
                .setFirstName(leadResponse.getFirstName()==null?"":leadResponse.getFirstName())
                .setLastName(leadResponse.getLastName()==null?"":leadResponse.getLastName())
                .setGender(leadResponse.getGender()==null?1:leadResponse.getGender())
                .setTitle(leadResponse.getTitle()==null?"":leadResponse.getTitle())
                .setEmail(leadResponse.getEmail()==null?"":leadResponse.getEmail())
                .setPhone(leadResponse.getPhone()==null?"":leadResponse.getPhone())
                .setWebsite(leadResponse.getWebsite()==null?"":leadResponse.getWebsite())
                .setCompany(leadResponse.getCompany()==null?"":leadResponse.getCompany())
                .setIsDelete(0)
                .build();
    }
}
