@Entity
public class User {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private String password;

    private Collection<Role> roles;

    private Boolean enabled;

    public User() {
    }

    public User(Long id,
                String firstName,
                String lastName,
                String email,
                String phoneNumber,
                String password,
                Collection<Role> roles,
                Boolean enabled) {

        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.roles = roles;
        this.enabled = enabled;
    }
}