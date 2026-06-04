import { Router } from 'express';
import {
  MEDALS,
  PUNISHMENT_CATEGORIES,
  RANKS,
  TAGS,
} from '../data/catalog.js';

const router = Router();

router.get('/', (_req, res) => {
  res.json({
    punishmentCategories: PUNISHMENT_CATEGORIES,
    ranks: RANKS,
    medals: MEDALS,
    tags: TAGS,
  });
});

export default router;
