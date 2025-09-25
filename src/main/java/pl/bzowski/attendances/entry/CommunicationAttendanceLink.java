package pl.bzowski.attendances.entry;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "communication_attendance_links")
public class CommunicationAttendanceLink extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(nullable = false)
    public UUID communicationId;

    @Column(nullable = false)
    public UUID attendanceEntryId;

    public CommunicationAttendanceLink() {
    }

    public CommunicationAttendanceLink(UUID communicationId, UUID attendanceEntryId) {
        this.communicationId = communicationId;
        this.attendanceEntryId = attendanceEntryId;
    }
}
