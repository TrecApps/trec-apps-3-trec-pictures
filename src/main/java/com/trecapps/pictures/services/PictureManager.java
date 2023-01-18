package com.trecapps.pictures.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.trecapps.auth.models.TcUser;
import com.trecapps.auth.services.UserStorageService;
import com.trecapps.pictures.models.*;
import com.trecapps.pictures.repos.BrandPicRepo;
import com.trecapps.pictures.repos.PictureRepo;
import com.trecapps.pictures.repos.ProfileRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

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
    BrandPicRepo brandPicRepo;

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

    private boolean hasAccess(String user, String brand, Picture pic)
    {
        if(pic.getUserId().equals(user))
            return true;

        PicturePermissions permissions = pictureStorageService.retrievePermissions(pic.getId());
        if(permissions == null || permissions.isPublic())
            return true;
        List<String> users = permissions.getUserId();
        List<String> brands = permissions.getBrandId();
        return (users != null && users.contains(user)) || (brands != null && brands.contains(brand));
    }

    public String addNewPicture(@NotNull String user, String name, @NotNull String ext,
                                  @NotNull String data, boolean isPublic, byte content)
    {
        Picture pic = new Picture();
        pic.setExtension(ext);
        pic.setContentRestriction(content);
        pic.setName(name);
        pic.setUserId(user);

        pic = pictureRepo.save(pic);

        pictureStorageService.uploadPicture(data, pic.getId(), ext, isPublic);

        return pic.getId();
    }

    public String addNewPicture(@NotNull String user, String name, @NotNull String ext,
                       @NotNull String data, boolean isPublic) {
        return addNewPicture(user,name, ext, data, isPublic, (byte)0);
    }

    public Picture retrievePictureObject(String id)
    {
        if(!pictureRepo.existsById(id))
        {
            return null;
        }
        return pictureRepo.getById(id);
    }

    public PictureData retrievePicture(@NotNull String id, String user, String brand)
    {
        PictureData ret = new PictureData();
        if(!pictureRepo.existsById(id))
        {
            ret.setError("404: Picture currently does not exist!");
            return ret;
        }

        Picture pic = pictureRepo.getById(id);

        byte contentThreshold = (byte) (topFree ? 0 : 1);

        boolean permitted = hasAccess(user, brand, pic) // First make sure the user has access, for not it will be true
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
        if(!profileRepo.existsById(user))
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

    public String setBrandProfile(@NotNull String brand, @NotNull String user, @NotNull String pictureId)
    {
        if(!pictureRepo.existsById(pictureId))
            return "404: Profile not available";

        Picture pic = pictureRepo.getById(pictureId);

        if(!user.equals(pic.getUserId()))
            return "403: Can't assign someone else's picture to your profile picture!";

        byte contentThreshold = (byte) (topFree ? 0 : 1);
        if(pic.getContentRestriction() > contentThreshold)
            return "403: Can't assign a NSFW picture as your profile picture!";

        BrandProfile prof = new BrandProfile();
        prof.setPictureId(pictureId);
        prof.setUserId(brand);

        brandPicRepo.save(prof);
        return "200: Profile Set!";
    }

    public PictureData getBrandProfile(@NotNull String brand)
    {
        PictureData ret = new PictureData();
        if(!brandPicRepo.existsById(brand))
        {
            ret.setError("Could not find Profile of User");
            return ret;
        }

        BrandProfile profile = brandPicRepo.getById(brand);

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

    public String getPicName(String id)
    {
        if(!pictureRepo.existsById(id))
            return null;
        Picture picture = pictureRepo.getById(id);
        return picture.getExtension();
    }

    public String getProfilePicName(String userId)
    {
        if(!profileRepo.existsById(userId))
            return null;
        Profile profile = profileRepo.getById(userId);
        if(!pictureRepo.existsById(profile.getPictureId()))
            return null;
        Picture picture = pictureRepo.getById(profile.getPictureId());
        return picture.getExtension();
    }

    public String getBrandProfilePicName(String userId)
    {
        if(!brandPicRepo.existsById(userId))
            return null;
        BrandProfile profile = brandPicRepo.getById(userId);
        if(!pictureRepo.existsById(profile.getPictureId()))
            return null;
        Picture picture = pictureRepo.getById(profile.getPictureId());
        return picture.getExtension();
    }

    public byte[] getPic(String id, String extension)
    {
        Picture picture = pictureRepo.getById(id);
        if(!picture.getExtension().equalsIgnoreCase(extension))
            return null;

        String fileName = String.format("%s.%s", picture.getId(), extension);
        LOGGER.info("Retrieving File Profile Pic '{}'", fileName);
        return pictureStorageService.retrieveImage(fileName);
    }

    public byte[] getProfilePic(String user, String extension)
    {
        if(!profileRepo.existsById(user))
            return null;
        Profile profile = profileRepo.getById(user);
        if(!pictureRepo.existsById(profile.getPictureId()))
            return null;
        Picture picture = pictureRepo.getById(profile.getPictureId());
        if(!picture.getExtension().equalsIgnoreCase(extension))
            return null;

        String fileName = String.format("%s.%s", picture.getId(), extension);
        LOGGER.info("Retrieving File Profile Pic '{}'", fileName);
        return pictureStorageService.retrieveImage(fileName);
    }

    public byte[] getBrandProfilePic(String user, String extension)
    {
        if(!brandPicRepo.existsById(user))
            return null;
        BrandProfile profile = brandPicRepo.getById(user);
        if(!pictureRepo.existsById(profile.getPictureId()))
            return null;
        Picture picture = pictureRepo.getById(profile.getPictureId());
        if(!picture.getExtension().equalsIgnoreCase(extension))
            return null;

        String fileName = String.format("%s.%s", picture.getId(), extension);
        LOGGER.info("Retrieving File Brand-Profile Pic '{}'", fileName);
        return pictureStorageService.retrieveImage(fileName);
    }

    public int makePicturePublic(@NotNull String user, @NotNull String id, boolean access)
    {
        if(!pictureRepo.existsById(id))
            return 404;
        Picture pic = pictureRepo.getById(id);
        if(!user.equals(pic.getUserId()))
            return 403;
        PicturePermissions permissions = pictureStorageService.retrievePermissions(id);
        permissions.setPublic(access);

        pictureStorageService.uploadPermissions(permissions, id);
        return 200;
    }

    public int addBrand(@NotNull String owner, @NotNull String id, @NotNull String brand)
    {
        if(!pictureRepo.existsById(id))
            return 404;
        Picture pic = pictureRepo.getById(id);
        if(!owner.equals(pic.getUserId()))
            return 403;
        PicturePermissions permissions = pictureStorageService.retrievePermissions(id);
        if(null == permissions)
            return 500;

        List<String> brands = permissions.getBrandId();
        if(null == brands)
            brands = new ArrayList<>();
        brands.add(brand);
        permissions.setBrandId(brands);
        pictureStorageService.uploadPermissions(permissions, id);
        return 200;
    }

    public int addUser(@NotNull String owner, @NotNull String id, @NotNull String user)
    {
        if(!pictureRepo.existsById(id))
            return 404;
        Picture pic = pictureRepo.getById(id);
        if(!owner.equals(pic.getUserId()))
            return 403;
        PicturePermissions permissions = pictureStorageService.retrievePermissions(id);
        if(null == permissions)
            return 500;

        List<String> users = permissions.getUserId();
        if(null == users)
            users = new ArrayList<>();
        users.add(user);
        permissions.setUserId(users);
        pictureStorageService.uploadPermissions(permissions, id);
        return 200;
    }

}
