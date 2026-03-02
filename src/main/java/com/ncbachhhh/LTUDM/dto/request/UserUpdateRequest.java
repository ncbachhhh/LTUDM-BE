package com.ncbachhhh.LTUDM.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.URL;

/**
 * DTO request cho việc cập nhật thông tin user.
 * Tất cả các trường đều optional - chỉ cập nhật những trường được gửi lên.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserUpdateRequest {
    @Size(min = 1, max = 100, message = "Display name must be between 1 and 100 characters")
    String display_name;

    @URL(message = "Avatar URL must be a valid URL")
    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    String avatar_url;
}
