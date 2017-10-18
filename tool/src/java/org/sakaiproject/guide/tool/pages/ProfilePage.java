package org.sakaiproject.guide.tool.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.SakaiProperties;

import java.io.IOException;
import java.util.*;

/**
 * The profile page which provides functionalities to create and update the user
 * profile
 *
 * @author George Kakarontzas (gkakaron@teilar.gr)
 * @author Johannes Zenkert (johannes.zenkert@uni-siegen.de)
 */

public class ProfilePage extends BasePage {
    private final static Logger log = Logger.getLogger(ProfilePage.class);
    private transient ResourceLoader rl = new ResourceLoader( "org.sakaiproject.guide.tool.MyApplication" );

    private final List<String> DIFFICULTIES =
            Arrays.asList(new String[]{
                    rl.getString("profile.listDifficulties.beginner"),
                    rl.getString("profile.listDifficulties.intermediate")});

    private final List<String> SPECIALIZATIONS = Arrays.asList(new String[]{
            rl.getString("profile.listJobInterests.health"),
            rl.getString("profile.listJobInterests.tourism"),
            rl.getString("profile.listJobInterests.science"),
            rl.getString("profile.listJobInterests.business")});

    private final List<String> GENDER =
            Arrays.asList(new String[]{
                    rl.getString("profile.gender.male"),
                    rl.getString("profile.gender.female")});

    private final List<String> EDUCATION_LEVEL =
            Arrays.asList(new String[]{
                    rl.getString("profile.educationLevel.no"),
                    rl.getString("profile.educationLevel.primary"),
                    rl.getString("profile.educationLevel.secondary"),
                    rl.getString("profile.educationLevel.bachelor"),
                    rl.getString("profile.educationLevel.master"),
                    rl.getString("profile.educationLevel.phd")});

    private final List<String> CURRENT_OCCUPATIONS =
            Arrays.asList(new String[]{
                    rl.getString("profile.currentOccupation.unemployed"),
                    rl.getString("profile.occupation.public"),
                    rl.getString("profile.occupation.private"),
                    rl.getString("profile.occupation.freelancer"),
                    rl.getString("profile.occupation.student") });


    private final List<String> TARGET_OCCUPATIONS =
            Arrays.asList(new String[]{
                    rl.getString("profile.occupation.public"),
                    rl.getString("profile.occupation.private"),
                    rl.getString("profile.occupation.freelancer"),
                    rl.getString("profile.occupation.student") });

    //make English selected by default
    //private String selectedLanguage = "English";
    //make Beginner difficulty selected by default
    private String selectedDifficulty = rl.getString("profile.listDifficulties.beginner");
    private String selectedSpecialization = "";
    private String selectedGender = rl.getString("profile.gender.male");
    private Integer selectedAge;
    private String selectedEducationLevel = rl.getString("profile.educationLevel.no");
    private String selectedInstructionLanguage = "";
    private String selectedCurrentOccupation = rl.getString("profile.currentOccupation.unemployed");
    private String selectedTargetOccupation = rl.getString("profile.occupation.public");
    private String selectedTargetPlaceOfResidence = "";

    private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        if (rl==null) {
            rl = new ResourceLoader( "org.sakaiproject.guide.tool.MyApplication" );
        }
    }

    public ProfilePage() {
        super();

        if (comalatUser != null) {

            String difficulty = comalatUser.getDifficulty();
            if (difficulty != null)
                selectedDifficulty = rl.getString(englishToProperty(difficulty));

            String specialization = comalatUser.getSpecialization();
            if (specialization != null)
                selectedSpecialization = rl.getString(englishToProperty(specialization));

            String gender = comalatUser.getGender();
            if (gender != null)
                selectedGender = rl.getString(englishToProperty(gender));

            Integer age = comalatUser.getAge();
            if (age != null)
                selectedAge = age;

            String educationLevel = comalatUser.getEducationLevel();
            if (educationLevel != null)
                selectedEducationLevel = rl.getString(englishToProperty(educationLevel));

            String instructionLanguage = comalatUser.getInstructionLanguage();
            if (instructionLanguage != null)
                selectedInstructionLanguage = rl.getString(englishToProperty(instructionLanguage));

            String currentOccupation = comalatUser.getCurrentOccupation();
            if (currentOccupation != null)
                selectedCurrentOccupation = rl.getString(englishToProperty(currentOccupation));

            String targetOccupation = comalatUser.getTargetOccupation();
            if (targetOccupation != null)
                selectedTargetOccupation = rl.getString(englishToProperty(targetOccupation));

            String targetPlaceOfResidence = comalatUser.getTargetPlaceOfResidence();
            if (targetPlaceOfResidence != null)
                selectedTargetPlaceOfResidence = targetPlaceOfResidence;

            disableLink(thirdLink);
        }

        WebMarkupContainer specializationContainer = new WebMarkupContainer("cont1");

        specializationContainer.setOutputMarkupPlaceholderTag(true);

        Label l = new Label("language", comalatUser.getLanguage());

        Label l1 = new Label("specializationsLabel",
                rl.getString("profile.labell1.your.job.specific.preference"));

        DropDownChoice<String> listJobInterests = new DropDownChoice<String>(
                "specializations", new PropertyModel<String>(this,
                "selectedSpecialization"), SPECIALIZATIONS);

        DropDownChoice<String> instructionLanguage = new DropDownChoice<String>(
                "instructionLanguage", new PropertyModel<String>(this,
                "selectedInstructionLanguage"), getLanguages());

        DropDownChoice<String> listDifficulties = new DropDownChoice<String>(
                "difficulties", new PropertyModel<String>(this,
                "selectedDifficulty"), DIFFICULTIES);

        listDifficulties.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
                // Hide the dropdownchoice for specializations based on this value
                // which can be "Beginner" or"Intermediate
                if (selectedDifficulty.equals(rl.getString("profile.listDifficulties.beginner"))) {
                    specializationContainer.setVisible(false);
                    String previouslySelectedSpecialization = selectedSpecialization;
                    selectedSpecialization = "";
                    try {
                        Site site = sakaiProxy.getCurrentSite();
                        String userId = sakaiProxy.getCurrentUserId();
                        //find the group that has the same name as the currently selected specialization
                        Collection<Group> groups = site.getGroups();
                        for (Group group : groups) {
                            String groupTitle = group.getTitle();
                            if (previouslySelectedSpecialization != null &&
                                    propertyToEnglish(previouslySelectedSpecialization).equals(groupTitle)) {
                                Member m = group.getMember(userId);
                                if (m != null) {
                                    info("Removed user from group " + group.getTitle());
                                    comalatUtilities.removeMemberFromGroup(site, group, userId);
                                }
                            }
                        }
                    } catch (IdUnusedException e) {
                        e.printStackTrace();
                    }
                    target.add(specializationContainer);
                } else {
                    specializationContainer.setVisible(true);
                    selectedInstructionLanguage = "";
                    target.add(specializationContainer);
                }
            }
        });

        DropDownChoice<String> gender = new DropDownChoice<String>(
                "gender", new PropertyModel<String>(this,
                "selectedGender"), GENDER);

        NumberTextField<Integer> age =
                new NumberTextField<Integer>("age", new PropertyModel<Integer>(this,
                        "selectedAge")).setMinimum(0).setMaximum(124);

        DropDownChoice<String> educationLevel = new DropDownChoice<String>(
                "educationLevel", new PropertyModel<String>(this,
                "selectedEducationLevel"), EDUCATION_LEVEL);

        DropDownChoice<String> currentOccupation = new DropDownChoice<String>(
                "currentOccupation", new PropertyModel<String>(this,
                "selectedCurrentOccupation"), CURRENT_OCCUPATIONS);

        DropDownChoice<String> targetOccupation = new DropDownChoice<String>(
                "targetOccupation", new PropertyModel<String>(this,
                "selectedTargetOccupation"), TARGET_OCCUPATIONS);

        DropDownChoice<String> targetPlaceOfResidence =
                new DropDownChoice<String>("targetPlaceOfResidence",
                        new PropertyModel<String>(this,
                                "selectedTargetPlaceOfResidence"),
                        getCountries());

        Form<?> form = new Form<Void>("form") {
            @Override
            protected void onSubmit() {
                comalatUser.setSpecialization(propertyToEnglish(selectedSpecialization));
                //comalatUser.setLanguage(selectedLanguage);
                comalatUser.setDifficulty(propertyToEnglish(selectedDifficulty));
                comalatUser.setGender(propertyToEnglish(selectedGender));
                comalatUser.setAge(selectedAge);
                comalatUser.setEducationLevel(propertyToEnglish(selectedEducationLevel));
                comalatUser.setInstructionLanguage(propertyToEnglish(selectedInstructionLanguage));
                comalatUser.setCurrentOccupation(propertyToEnglish(selectedCurrentOccupation));
                comalatUser.setTargetOccupation(propertyToEnglish(selectedTargetOccupation));
                comalatUser.setTargetPlaceOfResidence(selectedTargetPlaceOfResidence);

                projectLogic.saveComalatUser(comalatUser);


                try {
                    Site site = sakaiProxy.getCurrentSite();
                    Role siteRole = site.getUserRole(sakaiProxy.getCurrentUserId());
                    Collection<Group> groups = site.getGroups();
                    for (Group group : groups) {
                        Role role = group.getRole(siteRole.getId());
                        if (role == null) {
                            role = group.addRole("access");
                        }
                        //check to see if the user's selected specialization (LSP)
                        //is the group of this iteration and if it is then register the user if not
                        //already registered in this group
                        String groupTitle = group.getTitle();
                        String userId = sakaiProxy.getCurrentUserId();
                        if (comalatUser.getSpecialization().equals(groupTitle)) {
                            Member m = group.getMember(userId);
                            if (m == null) {
                                info("Registering user as a member of group " + group.getTitle());
                                comalatUtilities.addMemberToGroup(site, group, userId, role);

                            } else {
                                info("User is ALREADY a member of " + groupTitle);
                            }
                        }
                        //check to see if the user's selected instruction language
                        //is the group of this iteration and if it is then register the user if not
                        //already registered in this group
                        else if (comalatUser.getInstructionLanguage().equals(groupTitle)) {
                            Member m = group.getMember(userId);
                            if (m == null) {
                                info("Registering user as a member of group " + groupTitle);
                                comalatUtilities.addMemberToGroup(site, group, userId, role);
                            } else {
                                info("User is ALREADY a member of " + groupTitle);
                            }

                        }
                        //else if the group of this iteration is one of the instruction language or the
                        //LSP groups but the user has not selected this instruction language or LSP then
                        //remove user from this group if already registered
                        else if ("Arabic".equals(groupTitle) ||
                                "English".equals(groupTitle) ||
                                "Kurdish".equals(groupTitle) ||
                                "German".equals(groupTitle) ||
                                "Spanish".equals(groupTitle) ||
                                "Health".equals(groupTitle) ||
                                "Tourism and Hospitality".equals(groupTitle) ||
                                "Science and Technology".equals(groupTitle) ||
                                "Business and Professional Language".equals(groupTitle)) {
                            Member m = group.getMember(userId);
                            if (m != null) {
                                comalatUtilities.removeMemberFromGroup(site, group, userId);
                                info("Removed user from group " + groupTitle);
                            }
                        }
                    }
                } catch (IdUnusedException e) {
                    e.printStackTrace();
                } catch (RoleAlreadyDefinedException e) {
                    e.printStackTrace();
                }
            }
        };

        add(form);
        specializationContainer.add(l1);
        specializationContainer.add(listJobInterests);
        form.add(specializationContainer);
        form.add(instructionLanguage);
        form.add(l);
        form.add(listDifficulties);


        form.add(gender);
        form.add(age);
        form.add(educationLevel);

        form.add(currentOccupation);
        form.add(targetOccupation);
        form.add(targetPlaceOfResidence);

        //in the beginning
        specializationContainer.setVisible(!propertyToEnglish(selectedDifficulty).equals("Beginner"));

        // check if the user is initially in all base groups for every lesson
        checkGroupAssignmentForUser();

        Form<?> formDebug = new Form<Void>("formDebug") {
            @Override
            protected void onSubmit() {
                addToAllGroups();
            }
        };
        add(formDebug);

        Form<?> formDebug2 = new Form<Void>("formDebug2") {
            @Override
            protected void onSubmit() {
                addToAllNormalGroups();
            }
        };
        add(formDebug2);
    }

    private static List<String> getCountries() {
        List<String> countries = new ArrayList<>();
        String[] countryCodes = Locale.getISOCountries();
        for (String code : countryCodes) {
            Locale l = new Locale("", code);
            countries.add(
                    l.getDisplayCountry(new ResourceLoader().getLocale()));

        }
        Collections.sort(countries);
        return countries;
    }

    private List<String> getLanguages() {
        List<String> languages = new ArrayList<>();
        languages.add(rl.getString("profile.instructionLanguage.german"));
        languages.add(rl.getString("profile.instructionLanguage.english"));
        languages.add(rl.getString("profile.instructionLanguage.spanish"));
        languages.add(rl.getString("profile.instructionLanguage.arabic"));
        languages.add(rl.getString("profile.instructionLanguage.kurdish"));
        /*List<String> languages = new ArrayList<>();
        String[] languageCodes = Locale.getISOLanguages();
		for (String code : languageCodes) {
			Locale l = new Locale(code);
			languages.add(
					l.getDisplayLanguage(Locale.ENGLISH));

		}
		Collections.sort(languages);
		*/
        return languages;
    }

    /**
     * check the group assignment for the current user
     * assign to all base groups for every lesson if he has no group assignment
     */
    private void checkGroupAssignmentForUser() {
        //get all site groups
        Collection<Group> existingSiteGroups;
        Collection<Group> groupsWithUserAssigned;
        ArrayList<String> groupIdsToAddUserTo = new ArrayList<>();
        long start = System.currentTimeMillis();
        try {
            //get the groups a user is assigned to
            groupsWithUserAssigned = sakaiProxy.getCurrentSite().getGroupsWithMember(sakaiProxy.getCurrentUserId());
            if (groupsWithUserAssigned.isEmpty()) {
                existingSiteGroups = sakaiProxy.getCurrentSite().getGroups();
                for (Group g : existingSiteGroups) {
                    if (g.getTitle().contains("-")) {
                        String[] groupName = g.getTitle().split("-");
                        try {
                            if (groupName[2].equals("N") && g.getTitle().contains("INCOMPLETE") && groupName[6].equals("1")) {
                                //add the user to the right group
                                groupIdsToAddUserTo.add(g.getId());
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            //add user to all the groups
            comalatUtilities.addMemberToGroup(groupIdsToAddUserTo, sakaiProxy.getCurrentUserId());
        } catch (IdUnusedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        long time = end - start;
        log.info("### PROFILE PAGE: Time for groupassignment: " + time);
    }

    /**
     * ### debug ### add the user to all normal groups
     */
    private void addToAllNormalGroups() {
        Collection<Group> existingSiteGroups;
        ArrayList<String> groupIdsToAddUserTo = new ArrayList<>();
        try {
            existingSiteGroups = sakaiProxy.getCurrentSite().getGroups();
            //join all normal base groups
            for (Group g : existingSiteGroups) {
                String[] groupName = g.getTitle().split("-");
                StringBuilder baseGroup = new StringBuilder();
                for (int i = 0; i < groupName.length; i++) {
                    if (i == 6) {
                        baseGroup.append(groupName[i]);
                    } else {
                        baseGroup.append(groupName[i]).append("-");
                    }
                }
                if (g.getTitle().equals(baseGroup.toString()) || (g.getTitle().contains("INCOMPLETE") && groupName[2].equals("N"))) {
                    //add the user to the right group
                    groupIdsToAddUserTo.add(g.getId());
                }
            }
            comalatUtilities.addMemberToGroup(groupIdsToAddUserTo, sakaiProxy.getCurrentUserId());
        } catch (IdUnusedException e) {
            e.printStackTrace();
        }
    }

    /**
     * ### debug ### add the user to all groups
     */
    private void addToAllGroups() {
        comalatUtilities.addMemberToAllGroups(sakaiProxy.getCurrentUserId());
    }

    /**
     * Converter called prior to saving a property string in the database
     * in order to save the English word corresponding to the prooperty
     * regardless the current language
     * @param prop The property that needs to be saved
     * @return The corresponding Enlish term
     */
    private String propertyToEnglish(String prop) {
        if (prop!=null) {
            if (prop.equals(rl.getString("profile.listDifficulties.beginner")))
                return "Beginner";
            else if (prop.equals(rl.getString("profile.listDifficulties.intermediate")))
                return "Intermediate";
            else if (prop.equals(rl.getString("profile.listJobInterests.health")))
                return "Health";
            else if (prop.equals(rl.getString("profile.listJobInterests.tourism")))
                return "Tourism and Hospitality";
            else if (prop.equals(rl.getString("profile.listJobInterests.science")))
                return "Science and Technology";
            else if (prop.equals(rl.getString("profile.listJobInterests.business")))
                return "Business and Professional Language";
            else if (prop.equals(rl.getString("profile.gender.male")))
                return "Male";
            else if (prop.equals(rl.getString("profile.gender.female")))
                return "Female";
            else if (prop.equals(rl.getString("profile.educationLevel.no")))
                return "No Formal Education";
            else if (prop.equals(rl.getString("profile.educationLevel.primary")))
                return "Primary";
            else if (prop.equals(rl.getString("profile.educationLevel.secondary")))
                return "Secondary";
            else if (prop.equals(rl.getString("profile.educationLevel.bachelor")))
                return "Bachelor";
            else if (prop.equals(rl.getString("profile.educationLevel.master")))
                return "Master";
            else if (prop.equals(rl.getString("profile.educationLevel.phd")))
                return "PhD";
            else if (prop.equals(rl.getString("profile.currentOccupation.unemployed")))
                return "Unemployed";
            else if (prop.equals(rl.getString("profile.occupation.public")))
                return "Public Sector";
            else if (prop.equals(rl.getString("profile.occupation.private")))
                return "Private Sector";
            else if (prop.equals(rl.getString("profile.occupation.freelancer")))
                return "Freelancer";
            else if (prop.equals(rl.getString("profile.occupation.student")))
                return "Student";
            else if (prop.equals(rl.getString("profile.instructionLanguage.english")))
                return "English";
            else if (prop.equals(rl.getString("profile.instructionLanguage.german")))
                return "German";
            else if (prop.equals(rl.getString("profile.instructionLanguage.spanish")))
                return "Spanish";
            else if (prop.equals(rl.getString("profile.instructionLanguage.arabic")))
                return "Arabic";
            else if (prop.equals(rl.getString("profile.instructionLanguage.kurdish")))
                return "Kurdish";
            else if (prop.equals(""))
                return "";
            else
                return "";
        }
        else
            return "";

    }

    /**
     * Converter called after reading a property string from the database
     * in order to convert the English word stored in the database to the prooperty
     * correesponding to it
     * @param english The English word read from the database
     * @return The corresponding property key
     */
    private String englishToProperty(String english) {
        switch (english) {
            case "Beginner":
                return "profile.listDifficulties.beginner";
            case "Intermediate":
                return "profile.listDifficulties.intermediate";
            case "Health":
                return "profile.listJobInterests.health";
            case "Tourism and Hospitality":
                return "profile.listJobInterests.tourism";
            case "Science and Technology":
                return "profile.listJobInterests.science";
            case "Business and Professional Language":
                return "profile.listJobInterests.business";
            case "Male":
                return "profile.gender.male";
            case "Female":
                return "profile.gender.female";
            case "No Formal Education":
                return "profile.educationLevel.no";
            case "Primary":
                return "profile.educationLevel.primary";
            case "Secondary":
                return "profile.educationLevel.secondary";
            case "Bachelor":
                return "profile.educationLevel.bachelor";
            //this case is for backward compatibility before replacing Tertiary with Bachelor
            case "Tertiary":
                return "profile.educationLevel.bachelor";
            case "Master":
                return "profile.educationLevel.master";
            case "PhD":
                return "profile.educationLevel.phd";
            case "Unemployed":
                return "profile.currentOccupation.unemployed";
            case "Public Sector":
                return "profile.occupation.public";
            case "Private Sector":
                return "profile.occupation.private";
            case "Freelancer":
                return "profile.occupation.freelancer";
            case "Student":
                return "profile.occupation.student";
            case "English":
                return "profile.instructionLanguage.english";
            case "German":
                return "profile.instructionLanguage.german";
            case "Spanish":
                return "profile.instructionLanguage.spanish";
            case "Arabic":
                return "profile.instructionLanguage.arabic";
            case "Kurdish":
                return "profile.instructionLanguage.kurdish";
            case "":
                return"";
            default:
                return "UNKNOWN WORD";
        }
    }
}
