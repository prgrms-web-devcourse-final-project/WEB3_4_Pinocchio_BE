package sns.pinocchio.presentation.block.exception;

import org.springframework.http.HttpStatus;

public class BlockException extends RuntimeException {

    private final BlockErrorCode blockErrorCode;

    public BlockException(BlockErrorCode blockErrorCode) {
        super(blockErrorCode.getMessage());
        this.blockErrorCode = blockErrorCode;
    }

    public BlockErrorCode getBlockErrorCode() {
        return blockErrorCode;
    }

    public HttpStatus getStatus() {
        return blockErrorCode.getHttpStatus();
    }

    public String getCode() {
        return blockErrorCode.getCode();
    }
}