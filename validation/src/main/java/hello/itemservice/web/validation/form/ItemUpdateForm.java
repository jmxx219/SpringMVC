package hello.itemservice.web.validation.form;

import hello.itemservice.domain.item.UpdateCheck;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class ItemUpdateForm {
    @NotNull
    private Long id;

    @NotBlank
    private String itemName;

    @NotNull
    @Range(min=1000, max=1000000)
    private Integer price;

    //수정에서는 수량은 자유롭게 변경할 수 있음
    private Integer quantity;

}
