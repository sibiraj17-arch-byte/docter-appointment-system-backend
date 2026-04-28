DELETE FROM prescription_items
WHERE prescription_id IN (
    SELECT id
    FROM prescriptions
    WHERE doctor_id IS NOT NULL
      AND doctor_id NOT IN (SELECT id FROM doctors)
);

DELETE FROM medical_reports
WHERE doctor_id IS NOT NULL
  AND doctor_id NOT IN (SELECT id FROM doctors);

DELETE FROM prescriptions
WHERE doctor_id IS NOT NULL
  AND doctor_id NOT IN (SELECT id FROM doctors);

DELETE FROM reviews
WHERE doctor_id IS NOT NULL
  AND doctor_id NOT IN (SELECT id FROM doctors);

DELETE FROM payments
WHERE appointment_id IN (
    SELECT id
    FROM appointments
    WHERE doctor_id IS NOT NULL
      AND doctor_id NOT IN (SELECT id FROM doctors)
);

DELETE FROM medical_reports
WHERE appointment_id IN (
    SELECT id
    FROM appointments
    WHERE doctor_id IS NOT NULL
      AND doctor_id NOT IN (SELECT id FROM doctors)
);

DELETE FROM prescription_items
WHERE prescription_id IN (
    SELECT id
    FROM prescriptions
    WHERE appointment_id IN (
        SELECT id
        FROM appointments
        WHERE doctor_id IS NOT NULL
          AND doctor_id NOT IN (SELECT id FROM doctors)
    )
);

DELETE FROM prescriptions
WHERE appointment_id IN (
    SELECT id
    FROM appointments
    WHERE doctor_id IS NOT NULL
      AND doctor_id NOT IN (SELECT id FROM doctors)
);

DELETE FROM reviews
WHERE appointment_id IN (
    SELECT id
    FROM appointments
    WHERE doctor_id IS NOT NULL
      AND doctor_id NOT IN (SELECT id FROM doctors)
);

DELETE FROM appointments
WHERE doctor_id IS NOT NULL
  AND doctor_id NOT IN (SELECT id FROM doctors);

DELETE FROM availability_slots
WHERE doctor_id IS NOT NULL
  AND doctor_id NOT IN (SELECT id FROM doctors);
