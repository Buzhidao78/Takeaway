package com.bzy.takeaway.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bzy.takeaway.entity.ReviewVote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReviewVoteMapper extends BaseMapper<ReviewVote> {
    ReviewVote selectByReviewIdAndUserId(@Param("reviewId") Long reviewId, @Param("userId") Long userId);
}
