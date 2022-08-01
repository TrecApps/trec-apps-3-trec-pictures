package com.trecapps.pictures.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.trecapps.auth.models.TcUser;
import com.trecapps.auth.services.UserStorageService;
import com.trecapps.pictures.models.Picture;
import com.trecapps.pictures.models.PictureData;
import com.trecapps.pictures.models.Profile;
import com.trecapps.pictures.repos.PictureRepo;
import com.trecapps.pictures.repos.ProfileRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class PictureManager {

    Logger LOGGER = LoggerFactory.getLogger(PictureManager.class);

    @Autowired
    PictureStorageService pictureStorageService;

    @Autowired
    PictureRepo pictureRepo;

    @Autowired
    ProfileRepo profileRepo;

    @Autowired
    UserStorageService userStorageService;

    @Value("${topfreedom-safe:false}")
    boolean topFree;

    private boolean isAdult(String user)
    {
        if(null == user)
            return false;

        try {
            TcUser userDetails = userStorageService.retrieveUser(user);
            OffsetDateTime birthday = userDetails.getBirthday();

            return birthday.plus(18, ChronoUnit.YEARS).isBefore(OffsetDateTime.now());
        } catch (JsonProcessingException e) {
            LOGGER.error("Detected JSON Error retrieving TcUser {}!", user);
            return false;
        }
    }

    private boolean hasAccess(String user)
    {
        return true;
    }

    public void addNewPicture(@NotNull String user, String name, @NotNull String ext,
                                  @NotNull String data, byte content)
    {
        Picture pic = new Picture();
        pic.setExtension(ext);
        pic.setContentRestriction(content);
        pic.setName(name);
        pic.setUserId(user);

        pic = pictureRepo.save(pic);

        pictureStorageService.uploadPicture(data, pic.getId(), ext);
    }

    public void addNewPicture(@NotNull String user, String name, @NotNull String ext,
                       @NotNull String data) {
        addNewPicture(user,name, ext, data, (byte)0);
    }

    public PictureData retrievePicture(@NotNull String id, String user)
    {
        PictureData ret = new PictureData();
        if(!pictureRepo.existsById(id))
        {
            ret.setError("404: Picture currently does not exist!");
            return ret;
        }

        Picture pic = pictureRepo.getById(id);

        byte contentThreshold = (byte) (topFree ? 0 : 1);

        boolean permitted = hasAccess(user) // First make sure the user has access, for not it will be true
                && ((pic.getContentRestriction() <= contentThreshold) // Is there an age restriction
                || isAdult(user)); // If there is an age restriction, then make sure requester is an adult

        if(!permitted)
        {
            ret.setError(user == null ? "401: Requester does not have access" :
                    "403: Requester does not have access");
            return ret;
        }
        ret.setData(pictureStorageService.retrieveImage(id, pic.getExtension()));
        ret.setName(pic.getName());
        ret.setExt(pic.getExtension());
        return ret;
    }

    public String setProfile(@NotNull String user, @NotNull String pictureId)
    {
        if(!pictureRepo.existsById(pictureId))
            return "404: Profile not available";

        Picture pic = pictureRepo.getById(pictureId);

        if(!user.equals(pic.getUserId()))
            return "403: Can't assign someone else's picture to your profile picture!";

        byte contentThreshold = (byte) (topFree ? 0 : 1);
        if(pic.getContentRestriction() > contentThreshold)
            return "403: Can't assign a NSFW picture as your profile picture!";

        Profile prof = new Profile();
        prof.setPictureId(pictureId);
        prof.setUserId(user);

        profileRepo.save(prof);
        return "200: Profile Set!";
    }

    public PictureData getProfile(@NotNull String user)
    {
        PictureData ret = new PictureData();
        if(profileRepo.existsById(user))
        {
            ret.setError("Could not find Profile of User");
            return ret;
        }

        Profile profile = profileRepo.getById(user);

        if(!pictureRepo.existsById(profile.getPictureId()))
        {
            ret.setError("Could not find Image of User");
            return ret;
        }

        Picture pic = pictureRepo.getById(profile.getPictureId());
        ret.setData(pictureStorageService.retrieveImage(pic.getId(), pic.getExtension()));
        ret.setName(pic.getName());
        ret.setExt(pic.getExtension());
        return ret;
    }

}
