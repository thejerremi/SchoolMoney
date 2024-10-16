package com.paw.schoolMoney._class;

import com.paw.schoolMoney.child.ChildService;
import com.paw.schoolMoney.user.User;
import com.paw.schoolMoney.user.UserRepository;
import com.paw.schoolMoney.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class _ClassService {
    private final _ClassRepository repository;
    private final ChildService childService;
    private final UserService userService;
    private final UserRepository userRepository;

    public ResponseEntity<List<_ClassResponse>> getClassesByParentId(User user) {
        try{
            List<_ClassResponse> classes = repository.findByParentId(user.getId()).stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(classes);
        }catch (Exception e){
            return ResponseEntity.notFound().build();
        }
    }
    public ResponseEntity<_ClassResponse> getClassById(int id) {
        try{
            Optional<_Class> _class = repository.findById(id);
            return _class.map(aClass -> ResponseEntity.ok(convertToResponse(aClass))).orElseGet(() -> ResponseEntity.notFound().build());
        }catch (Exception e){
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<?> addClass(User user, _ClassRequest request) {
        Optional<_Class> existingClass = repository.findByClassName(request.getClassName());

        if (existingClass.isPresent()) {
            User treasurer = existingClass.get().getTreasurer();

            Map<String, String> response = new HashMap<>();
            response.put("message", "Class with this name already exists.");
            response.put("treasurerFirstName", treasurer.getFirstname());
            response.put("treasurerLastName", treasurer.getLastname());
            response.put("treasurerEmail", treasurer.getEmail());

            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        _Class newClass = _Class.builder()
                .className(request.getClassName())
                .treasurer(user)
                .children(new ArrayList<>())
                .parents(new ArrayList<>())
                .build();

        newClass.getParents().add(user);
        repository.save(newClass);

        return ResponseEntity.ok("Class added successfully");
    }


    public _ClassResponse convertToResponse(_Class _class) {
        return _ClassResponse.builder()
                .id(_class.getId())
                .className(_class.getClassName())
                .treasurer(userService.convertToResponse(_class.getTreasurer()))
                .children(childService.convertToResponse(_class.getChildren()))
                .parents(userService.convertToResponse(_class.getParents()))
                .build();
    }

    public ResponseEntity<?> deleteClass(User user, int id) {
        try {
            _Class _class = repository.findById(id).orElse(null);
            if (_class != null) {
                if (!user.getId().equals(_class.getTreasurer().getId())) {
                    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Only the treasurer can delete the class.");
                }
                _class.getParents().clear();
                repository.delete(_class);
                return ResponseEntity.ok().body("Class deleted successfully.");
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Class not found.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete class.");
        }
    }

    public ResponseEntity<?> addParentToClass(User user, int parentId, int classId) {
        try {
            _Class _class = repository.findById(classId).orElse(null);
            if (_class != null) {
                // Sprawdzenie, czy użytkownik to skarbnik
                if (!user.getId().equals(_class.getTreasurer().getId())) {
                    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                            .body("Only the treasurer can add a parent to the class.");
                }

                Optional<User> parent = userRepository.findById(parentId);
                if (parent.isPresent()) {
                    if (_class.getParents().contains(parent.get())) {
                        return ResponseEntity.status(HttpStatus.CONFLICT) // Status 409 - Conflict
                                .body("Parent is already in this class.");
                    }
                    _class.getParents().add(parent.get());
                    repository.save(_class);

                    return ResponseEntity.ok().body("Parent added to the class successfully.");
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parent not found.");
                }
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Class not found.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add parent to the class.");
        }
    }

    public ResponseEntity<?> updateClassName(User user, String id, String newClassName) {
        try {
            _Class _class = repository.findById(Integer.valueOf(id))
                    .orElseThrow(() -> new EntityNotFoundException("Class not found with id " + id));
            if (!user.getId().equals(_class.getTreasurer().getId())) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                        .body("Only the treasurer can change class name.");
            }
            _class.setClassName(newClassName);
            repository.save(_class);
            return ResponseEntity.ok().body("Class name was updated.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update class name.");
        }
    }

    public ResponseEntity<?> updateClassTreasurer(User user, String classId, String newTreasurerId) {
        try {
            _Class _class = repository.findById(Integer.valueOf(classId))
                    .orElseThrow(() -> new EntityNotFoundException("Class not found with id " + classId));

            if (!user.getId().equals(_class.getTreasurer().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Only the current treasurer can change the successor.");
            }

            User newTreasurer = userRepository.findById(Integer.valueOf(newTreasurerId))
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id " + newTreasurerId));

            if (!_class.getParents().contains(newTreasurer)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("The new treasurer must be one of the parents in the class.");
            }

            _class.setTreasurer(newTreasurer);
            repository.save(_class);

            return ResponseEntity.ok().body("Class treasurer was updated successfully.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update class treasurer due to an internal error.");
        }
    }

    public ResponseEntity<?> deleteParent(User user, int parentId, int classId) {
        try {
            _Class _class = repository.findById(classId)
                    .orElseThrow(() -> new EntityNotFoundException("Class not found with id " + classId));

            User parentToDelete = userRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Parent not found with id " + parentId));

            // Sprawdzenie, czy użytkownik próbuje usunąć samego siebie, będąc skarbnikiem
            if (user.getId().equals(_class.getTreasurer().getId()) && user.getId().equals(parentToDelete.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("As a treasurer, you cannot remove yourself from the class.");
            }

            // Sprawdzenie, czy użytkownik jest skarbnikiem lub usuwa samego siebie
            if (!user.getId().equals(_class.getTreasurer().getId()) && !user.getId().equals(parentToDelete.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Only the treasurer can remove other parents, or you can remove yourself.");
            }

            _class.getParents().remove(parentToDelete);

            repository.save(_class);
            return ResponseEntity.ok().body("Parent removed successfully from the class.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to remove parent.");
        }


    }
    public List<_ClassResponse> findAllClassesByTreasurer(int id){
        return repository.findAllByTreasurerId(id).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

}