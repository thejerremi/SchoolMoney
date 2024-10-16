package com.paw.schoolMoney.child;

import com.paw.schoolMoney._class._Class;
import com.paw.schoolMoney.child.Child;
import com.paw.schoolMoney._class._ClassRepository;
import com.paw.schoolMoney.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChildService {
    private final ChildRepository repository;
    private final _ClassRepository classRepository;

    public ResponseEntity<?> addChild(User user, ChildRequest request) {
        _Class _class = null;
        if(request.get_class() != null) {
            _class = classRepository.findById(request.get_class()).orElse(null);
        }
        var child = Child.builder()
                .firstname(request.getFirstName())
                .lastname(request.getLastName())
                .dob(request.getDob())
                .accountNumber(String.valueOf(new java.util.Random().ints(26, 0, 10)
                        .mapToObj(Integer::toString).collect(java.util.stream.Collectors.joining())))
                ._class(_class)
                .user(user)
                .build();
        repository.save(child);
        return ResponseEntity.ok().build();
    }

    public List<ChildResponse> getChildren(Integer userId) {
        try{
            return repository.findChildsByUserId(userId).stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        }catch(Exception e){
            throw new IllegalStateException(e);
        }
    }

    public ChildResponse convertToResponse(Child child) {
        return ChildResponse.builder()
                .id(child.getId())
                .firstName(child.getFirstname())
                .lastName(child.getLastname())
                .dob(child.getDob())
                .accountNumber(child.getAccountNumber())
                ._class(Optional.ofNullable(child.get_class()).map(_Class::getId).orElse(null))
                .parentId(child.user.getId())
                .build();
    }

    public List<ChildResponse> convertToResponse(List<Child> children) {
        return children.stream()
                .map(child -> ChildResponse.builder()
                        .id(child.getId())
                        .firstName(child.getFirstname())
                        .lastName(child.getLastname())
                        .dob(child.getDob())
                        .accountNumber(child.getAccountNumber())
                        ._class(Optional.ofNullable(child.get_class()).map(_Class::getId).orElse(null))
                        .parentId(child.user.getId())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public ResponseEntity<?> deleteChild(User user, int id) {
        try {
            repository.deleteChildByIdAndUserId(id, user.getId());
            return ResponseEntity.ok().body("Child deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete child.");
        }
    }

    public ResponseEntity<?> editChild(User user, ChildRequest request) {
        try{
            var child = repository.findChildById(request.getId());
            if(!Objects.equals(child.getUser().getId(), user.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to edit this child.");
            }
            if(request.getFirstName() != null)
                child.setFirstname(request.getFirstName());
            if(request.getLastName() != null)
                child.setLastname(request.getLastName());
            if(request.getDob() != null)
                child.setDob(request.getDob());
            if(request.get_class() != null) {
                Optional<_Class> _class = classRepository.findById(request.get_class());
                _class.ifPresent(child::set_class);
            }
            repository.save(child);
            return ResponseEntity.ok().body("Child edited successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to edit child.");
        }
    }

    public ResponseEntity<?> getChildrenByClassId(User user, Integer classId) {
        try{
            return ResponseEntity.ok(repository.findChildsBy_classId(classId)
                    .stream()
                    .map(this::convertToResponse)
                    .toList());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get children by class id.");
        }
    }

    public ResponseEntity<?> assignClassToChild(User user, Integer childId, Integer classId) {
        try{
            Child child = repository.findById(childId)
                    .orElseThrow(() -> new EntityNotFoundException("Child not found with id " + childId));
            if(!Objects.equals(child.getUser().getId(), user.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to edit this child.");
            }
            _Class _class = classRepository.findById(classId)
                    .orElseThrow(() -> new EntityNotFoundException("Class not found with id " + classId));

            child.set_class(_class);
            repository.save(child);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to assign child to class.");

        }
    }

    public ResponseEntity<?> removeClassFromChild(User user, Integer childId) {
        try{
            Child child = repository.findById(childId)
                    .orElseThrow(() -> new EntityNotFoundException("Child not found with id " + childId));
            _Class _class = classRepository.findById(child.get_class().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Class not found"));
            if(!Objects.equals(child.getUser().getId(), user.getId()) || !Objects.equals(_class.getTreasurer().getId(), user.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to edit this child.");
            }
            child.set_class(null);
            repository.save(child);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete class from child.");
        }
    }
    public String getChildFullName(Child child) {
        if (child == null) {
            return "Dziecko nieznane";
        }
        return child.getFirstname() + " " + child.getLastname();
    }
}
