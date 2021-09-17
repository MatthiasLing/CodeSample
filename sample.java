//Removed imports to save lines

@Service
public class firebaseService {

//    The function:
//    clean the db
//    add a new day to the days array if needed
//    returns the user object to the frontend
    public User getUser(String email) throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();

        CollectionReference users = db.collection("users");
        Query query = users.whereEqualTo("email", email);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        int needNew = 1;

        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            System.out.println(document.get("email") + " found!");
            User user = document.toObject(User.class);

            LocalDate currDate = new Date(System.currentTimeMillis()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            ArrayList<Day> updatedDays = new ArrayList<Day>();
            ZoneId defaultZoneId = ZoneId.systemDefault();


            for (Day day : user.getDays()){
                LocalDate tempDate = day.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                if (tempDate.getYear() == currDate.getYear()
                        && tempDate.getDayOfMonth() == currDate.getDayOfMonth()
                        && tempDate.getMonth() == currDate.getMonth()) {
                        needNew = 0;
                    }

                    if (DAYS.between(tempDate, currDate) < 30) {
                        System.out.println("Day found");
                        updatedDays.add(day);
                    }
            }
            if (needNew == 1){
                Day latest = new Day(Date.from(currDate.atStartOfDay(defaultZoneId).toInstant()),
                        user.getHabits(),user.getHabits().size());
                updatedDays.add(latest);
            }
            user.setDays(updatedDays);

            long between = DAYS.between(user.getLastLoggedIn().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), currDate);

            if ( between > 1){
                // lost streak
                user.setStreak(0);
            }else if (between == 1){
                // Adding to streak
                user.setStreak(user.getStreak() + 1);
            }
            // else, same day login

            user.setLastLoggedIn(Date.from(currDate.atStartOfDay(defaultZoneId).toInstant()));

            ApiFuture<WriteResult> collectionsApiFuture = db.collection("users").document(email).set(user);

            return user;
        }
        System.out.println("No user found");
        return null;

    }
    public String saveUser(User user) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        user.setLastLoggedIn(new Date(System.currentTimeMillis()));
        ApiFuture<WriteResult> collectionsApiFuture = db.collection("users").document(user.getName()).set(user);
        return collectionsApiFuture.get().getUpdateTime().toString();
    }
}
