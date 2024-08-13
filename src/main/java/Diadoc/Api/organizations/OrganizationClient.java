package Diadoc.Api.organizations;

import Diadoc.Api.exceptions.DiadocException;
import Diadoc.Api.exceptions.DiadocSdkException;
import Diadoc.Api.helpers.Tools;
import Diadoc.Api.httpClient.DiadocHttpClient;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;

import static Diadoc.Api.Proto.GetOrganizationsByInnListProtos.*;
import static Diadoc.Api.Proto.OrganizationProtos.*;
import static Diadoc.Api.Proto.OrganizationUserPermissionsProtos.OrganizationUserPermissions;
import static Diadoc.Api.Proto.OrganizationUserProtos.OrganizationUsersList;
import static Diadoc.Api.Proto.Organizations.OrganizationFeaturesProtos.OrganizationFeatures;
import static Diadoc.Api.Proto.Registration.RegistrationRequestProtos.*;

public class OrganizationClient {
    private DiadocHttpClient diadocHttpClient;

    public OrganizationClient(DiadocHttpClient diadocHttpClient) {
        this.diadocHttpClient = diadocHttpClient;
    }

    public Box getBox(String boxId) throws DiadocSdkException {
        if (boxId == null)
            throw new IllegalArgumentException("boxId");
        try {
            var request = RequestBuilder.get(
                    new URIBuilder(diadocHttpClient.getBaseUrl())
                            .setPath("/GetBox")
                            .addParameter("boxId", boxId)
                            .build());
            return Box.parseFrom(diadocHttpClient.performRequest(request));
        } catch (URISyntaxException | IOException e) {
            throw new DiadocSdkException(e);
        }
    }

    public Organization getOrganizationById(String orgId) throws DiadocSdkException {
        if (orgId == null || orgId.isEmpty()) {
            throw new IllegalArgumentException("orgId");
        }
        return getOrganization("orgId", orgId);
    }

    public Organization getOrganizationByInn(String inn) throws DiadocSdkException {
        if (inn == null || inn.isEmpty()) {
            throw new IllegalArgumentException("inn");
        }
        return getOrganization("inn", inn);
    }

    public Organization getOrganizationByFnsParticipantId(String fnsParticipantId) throws DiadocSdkException {
        if (fnsParticipantId == null || fnsParticipantId.isEmpty()) {
            throw new IllegalArgumentException("fnsParticipantId");
        }
        return getOrganization("fnsParticipantId", fnsParticipantId);
    }

    public OrganizationWithCounteragentStatus[] getOrganizationsByInnList(String myOrgId, Iterable<String> innList) throws DiadocSdkException {
        var request = GetOrganizationsByInnListRequest.newBuilder();
        request.addAllInnList(innList);
        return getOrganizationsByInnList(myOrgId, request.build());
    }

    public OrganizationWithCounteragentStatus[] getOrganizationsByInnList(String myOrgId, GetOrganizationsByInnListRequest innListRequest) throws DiadocSdkException {
        if (myOrgId == null)
            throw new IllegalArgumentException("myOrgId");
        if (innListRequest == null)
            throw new IllegalArgumentException("innListRequest");
        try {
            var request = RequestBuilder.post(
                    new URIBuilder(diadocHttpClient.getBaseUrl())
                            .setPath("/GetOrganizationsByInnList")
                            .addParameter("myOrgId", myOrgId)
                            .build())
                    .setEntity(new ByteArrayEntity(innListRequest.toByteArray()));


            var response = GetOrganizationsByInnListResponse.parseFrom(diadocHttpClient.performRequest(request));
            return response.getOrganizationsList().toArray(new OrganizationWithCounteragentStatus[0]);

        } catch (URISyntaxException | IOException e) {
            throw new DiadocSdkException(e);
        }
    }

    public Organization[] getOrganizationsByInnList(Iterable<String> innList) throws DiadocSdkException {
        var request = GetOrganizationsByInnListRequest.newBuilder();
        request.addAllInnList(innList);
        return getOrganizationsByInnList(request.build());
    }

    public Organization[] getOrganizationsByInnList(GetOrganizationsByInnListRequest innListRequest) throws DiadocSdkException {
        if (innListRequest == null) {
            throw new IllegalArgumentException("innListRequest");
        }
        try {
            var request = RequestBuilder.post(
                    new URIBuilder(diadocHttpClient.getBaseUrl())
                            .setPath("/GetOrganizationsByInnList")
                            .build())
                    .setEntity(new ByteArrayEntity(innListRequest.toByteArray()));
            var response = GetOrganizationsByInnListResponse.parseFrom(diadocHttpClient.performRequest(request));
            return response
                    .getOrganizationsList()
                    .stream()
                    .map(OrganizationWithCounteragentStatus::getOrganization)
                    .toArray(Organization[]::new);
        } catch (URISyntaxException | IOException e) {
            throw new DiadocSdkException(e);
        }
    }

    public OrganizationList getOrganizationsByInnKpp(String inn, @Nullable String kpp, boolean includeRelations) throws DiadocSdkException {
        if (inn == null) {
            throw new IllegalArgumentException("inn");
        }
        try {
            var url = new URIBuilder(diadocHttpClient.getBaseUrl())
                    .setPath("/GetOrganizationsByInnKpp")
                    .addParameter("inn", inn);

            if (!Tools.isNullOrEmpty(kpp)) {
                url.addParameter("kpp", kpp);
            }

            if (includeRelations) {
                url.addParameter("includeRelations", "true");
            }
            var request = RequestBuilder.get(url.build());
            return OrganizationList.parseFrom(diadocHttpClient.performRequest(request));
        } catch (URISyntaxException | IOException e) {
            throw new DiadocSdkException(e);
        }
    }

    public OrganizationList getOrganizationsByInnKpp(String inn, @Nullable String kpp) throws DiadocSdkException {
        return getOrganizationsByInnKpp(inn, kpp, false);
    }

    public OrganizationList getMyOrganizations(boolean autoRegister) throws DiadocSdkException {
        try {
            var url = new URIBuilder(diadocHttpClient.getBaseUrl())
                    .setPath("/GetMyOrganizations");

            if (!autoRegister) {
                url.addParameter("autoRegister", Boolean.toString(false));
            }
            var request = RequestBuilder.get(url.build());
            return OrganizationList.parseFrom(diadocHttpClient.performRequest(request));
        } catch (URISyntaxException | IOException e) {
            throw new DiadocSdkException(e);
        }
    }

    public OrganizationList getMyOrganizations() throws DiadocSdkException {
        return getMyOrganizations(true);
    }

    /**
     * Use getOrganizationUsersV2 instead
     */

    @Deprecated
    public OrganizationUsersList getOrganizationUsers(String orgId) throws DiadocSdkException {
        try {
            var request = RequestBuilder.get(
                    new URIBuilder(diadocHttpClient.getBaseUrl())
                            .setPath("/GetOrganizationUsers")
                            .addParameter("orgId", orgId)
                            .build());
            return OrganizationUsersList.parseFrom(diadocHttpClient.performRequest(request));
        } catch (URISyntaxException | IOException e) {
            throw new DiadocSdkException(e);
        }
    }
    
    public OrganizationUsersList getOrganizationUsersV2(String boxId) throws DiadocSdkException {
        try {
            var request = RequestBuilder.get(
                    new URIBuilder(diadocHttpClient.getBaseUrl())
                            .setPath("/V2/GetOrganizationUsers")
                            .addParameter("boxId", boxId)
                            .build());
            return OrganizationUsersList.parseFrom(diadocHttpClient.performRequest(request));
        } catch (URISyntaxException | IOException e) {
            throw new DiadocSdkException(e);
        }
    }

    public OrganizationUserPermissions getMyPermissions(String orgId) throws DiadocSdkException {
        if (orgId == null) {
            throw new IllegalArgumentException("orgId");
        }

        try {
            var request = RequestBuilder.get(
                    new URIBuilder(diadocHttpClient.getBaseUrl())
                            .setPath("/GetMyPermissions")
                            .addParameter("orgId", orgId)
                            .build());
            return OrganizationUserPermissions.parseFrom(diadocHttpClient.performRequest(request));
        } catch (URISyntaxException | IOException e) {
            throw new DiadocSdkException(e);
        }
    }

    private Organization getOrganization(String name, String value) throws DiadocSdkException {
        try {
            var request = RequestBuilder.get(
                    new URIBuilder(
                            diadocHttpClient.getBaseUrl())
                            .setPath("/GetOrganization")
                            .addParameter(name, value)
                            .build());

            return Organization.parseFrom(diadocHttpClient.performRequest(request));
        } catch (URISyntaxException | IOException e) {
            throw new DiadocSdkException(e);
        }
    }

    public OrganizationFeatures getOrganizationFeatures(String boxId) throws DiadocSdkException {
        if (boxId == null) {
            throw new IllegalArgumentException("boxId");
        }

        try {
            var request = RequestBuilder.get(
                    new URIBuilder(
                            diadocHttpClient.getBaseUrl())
                            .setPath("/GetOrganizationFeatures")
                            .addParameter("boxId", boxId)
                            .build());

            return OrganizationFeatures.parseFrom(diadocHttpClient.performRequest(request));
        } catch (URISyntaxException | IOException e) {
            throw new DiadocSdkException(e);
        }
    }

    public RegistrationResponse register(RegistrationRequest registrationRequest) throws DiadocSdkException {
        try {
            var request = RequestBuilder.post(
                    new URIBuilder(diadocHttpClient.getBaseUrl())
                            .setPath("/Register")
                            .build())
                    .setEntity(new ByteArrayEntity(registrationRequest.toByteArray()));
            return RegistrationResponse.parseFrom(diadocHttpClient.performRequest(request));

        } catch (URISyntaxException | IOException e) {
            throw new DiadocSdkException(e);
        }
    }

    public void registerConfirm(RegistrationConfirmRequest registrationConfirmRequest) throws DiadocSdkException {
        try {
            var request = RequestBuilder.post(
                    new URIBuilder(diadocHttpClient.getBaseUrl())
                            .setPath("/Register")
                            .build())
                    .setEntity(new ByteArrayEntity(registrationConfirmRequest.toByteArray()));
            diadocHttpClient.performRequest(request);

        } catch (URISyntaxException | IOException e) {
            throw new DiadocSdkException(e);
        }
    }

    public boolean canSendInvoice(String boxId, byte[] certBytes) throws DiadocSdkException {
        if (Tools.isNullOrEmpty(boxId)) {
            throw new IllegalArgumentException("boxId");
        }
        if (certBytes == null || certBytes.length == 0) {
            throw new IllegalArgumentException("certBytes");
        }

        try {
            var request = RequestBuilder.post(
                    new URIBuilder(diadocHttpClient.getBaseUrl())
                            .setPath("/CanSendInvoice")
                            .addParameter("boxId", boxId)
                            .build())
                    .setEntity(new ByteArrayEntity(certBytes));

            var response = diadocHttpClient.getResponse(request);
            switch (response.getStatusCode()) {
                case HttpStatus.SC_OK:
                    return true;
                case HttpStatus.SC_FORBIDDEN:
                    return false;
                default:
                    throw new DiadocException(response.getReason(), response.getStatusCode());
            }
        } catch (URISyntaxException | IOException | DiadocException e) {
            throw new DiadocSdkException(e);
        }
    }

}
